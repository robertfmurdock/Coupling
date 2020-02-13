package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.dynamo.external.DynamoDB
import com.zegreatrob.coupling.dynamo.external.config
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlinx.coroutines.await
import kotlinx.coroutines.yield
import kotlin.js.Json
import kotlin.js.json


suspend fun dynamoTribeRepository(): DynamoTribeRepository {
    config.update(
        json(
            "region" to "us-east-1",
            "endpoint" to "http://localhost:8000"
        )
    )
    val dynamoDB = DynamoDB()

    if (!checkTableExists(dynamoDB)) {
        createTribeTable(dynamoDB)

        while (tribeTableStatus(dynamoDB) != "ACTIVE") {
            yield()
        }
    }

    return DynamoTribeRepository(dynamoDB)
}

private suspend fun tribeTableStatus(dynamoDB: DynamoDB) = describeTribeTable(dynamoDB)
    .let { it["Table"].unsafeCast<Json>()["TableStatus"] }

private suspend fun checkTableExists(dynamoDB: DynamoDB) = try {
    describeTribeTable(dynamoDB)
    true
} catch (throwable: Throwable) {
    false
}

private suspend fun describeTribeTable(dynamoDB: DynamoDB): Json = dynamoDB.describeTable(json("TableName" to "TRIBE"))
    .promise()
    .await()

private suspend fun createTribeTable(dynamoDB: DynamoDB) {
    dynamoDB.createTable(
        json(
            "TableName" to DynamoTribeRepository.tableName,
            "KeySchema" to arrayOf(
                json(
                    "AttributeName" to "id",
                    "KeyType" to "HASH"
                ),
                json(
                    "AttributeName" to "timestamp",
                    "KeyType" to "RANGE"
                )
            ),
            "AttributeDefinitions" to arrayOf(
                json(
                    "AttributeName" to "id",
                    "AttributeType" to "S"
                ),
                json(
                    "AttributeName" to "timestamp",
                    "AttributeType" to "S"
                )
            ),
            "BillingMode" to "PAY_PER_REQUEST"
        )
    ).promise().await()
}


class DynamoTribeRepository(override val dynamoDB: DynamoDB) : TribeRepository, DynamoItemDeleteSyntax,
    DynamoItemGetSyntax, DynamoItemListGetSyntax, DynamoItemPutSyntax {

    companion object {
        const val tableName = "TRIBE"
    }

    override val tableName: String get() = Companion.tableName

    override suspend fun getTribe(tribeId: TribeId) = performGetSingleItemQuery(tribeId.value)?.toTribe()

    override suspend fun getTribes() = scanForItemList().map { it.toTribe() }

    override suspend fun save(tribe: Tribe) = performPutItem(tribe.asDynamoJson())

    override suspend fun delete(tribeId: TribeId) = performDelete(tribeId.value)

    private fun Json.toTribe() = Tribe(
        id = TribeId(getDynamoStringValue("id")!!),
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        pairingRule = PairingRule.fromValue(
            getDynamoNumberValue("pairingRule")?.toInt()
        ),
        defaultBadgeName = getDynamoStringValue("defaultBadgeName"),
        alternateBadgeName = getDynamoStringValue("alternateBadgeName"),
        badgesEnabled = getDynamoBoolValue("badgesEnabled") ?: false,
        callSignsEnabled = getDynamoBoolValue("callSignsEnabled") ?: false,
        animationEnabled = getDynamoBoolValue("animationEnabled") ?: false,
        animationSpeed = getDynamoNumberValue("animationSpeed")?.toDouble() ?: 1.0
    )

    private fun Tribe.asDynamoJson() = json(
        "id" to id.value.dynamoString(),
        "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
        "name" to name.dynamoString(),
        "email" to email.dynamoString(),
        "pairingRule" to PairingRule.toValue(pairingRule).dynamoNumber(),
        "defaultBadgeName" to defaultBadgeName.dynamoString(),
        "alternateBadgeName" to alternateBadgeName.dynamoString(),
        "badgesEnabled" to badgesEnabled.dynamoBool(),
        "callSignsEnabled" to callSignsEnabled.dynamoBool(),
        "animationEnabled" to animationEnabled.dynamoBool(),
        "animationSpeed" to animationSpeed.dynamoNumber()
    )

}

