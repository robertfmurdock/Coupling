package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.soywiz.klock.ISO8601
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


class DynamoTribeRepository(private val dynamoDB: DynamoDB) : TribeRepository {

    companion object {
        const val tableName = "TRIBE"
    }

    override suspend fun getTribe(tribeId: TribeId) = dynamoDB.query(singleTribeQuery(tribeId)).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        .also { println("first item is ${JSON.stringify(it)}") }
        ?.let(::excludeDeleted)
        ?.toTribe()

    private fun singleTribeQuery(tribeId: TribeId) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":id" to json("S" to tribeId.value)),
        "KeyConditionExpression" to "id = :id"
    )

    private fun excludeDeleted(it: Json) = if (it.getDynamoBoolValue("isDeleted") == true)
        null
    else
        it

    override suspend fun getTribes() = dynamoDB.scan(allTribesScan()).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .groupBy { it.getDynamoStringValue("id") }
        .map { it.value.last() }
        .mapNotNull(::excludeDeleted)
        .map { it.toTribe() }

    private fun allTribesScan() = json("TableName" to tableName)

    private fun Json.itemsNode() = this["Items"].unsafeCast<Array<Json>>()

    private fun Array<Json>.sortByRecordTimestamp() = sortedBy { it.getDynamoStringValue("timestamp") }

    override suspend fun save(tribe: Tribe) = dynamoDB.putItem(
        json(
            "TableName" to tableName,
            "Item" to tribe.asDynamoJson()
        )
    ).promise().await()

    override suspend fun delete(tribeId: TribeId) = try {
        dynamoDB.putItem(deleteTableItemJson(tribeId)).promise().await()
        true
    } catch (uhOh: Throwable) {
        false
    }

    private fun deleteTableItemJson(tribeId: TribeId) = json(
        "TableName" to tableName,
        "Item" to json(
            "id" to tribeId.value.dynamoString(),
            "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
            "isDeleted" to true.dynamoBool()
        )
    )

    private fun Json.toTribe(): Tribe = Tribe(
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

    private fun Json.getDynamoStringValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("S")?.unsafeCast<String?>()

    private fun Json.getDynamoNumberValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("N")?.unsafeCast<String?>()

    private fun Json.getDynamoBoolValue(property: String) =
        this[property].unsafeCast<Json?>()?.get("BOOL")?.unsafeCast<Boolean?>()

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

    private fun DateTime.isoWithMillis() = "${format(ISO8601.DATETIME_COMPLETE)}.${format("SSS")}"

    private fun String.dynamoString() = json("S" to this)
    private fun String?.dynamoString() = this?.dynamoString() ?: dynamoNull()
    private fun Number.dynamoNumber(): Json = json("N" to "$this")
    private fun Boolean.dynamoBool() = json("BOOL" to this)
    private fun dynamoNull(): Json = json("NULL" to "true")

}


