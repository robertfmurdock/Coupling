package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoTribeRepository private constructor() : TribeRepository,
    DynamoItemGetSyntax,
    DynamoItemPutSyntax,
    DynamoItemListGetSyntax,
    DynamoItemDeleteSyntax,
    DynamoTableNameSyntax by Companion,
    DynamoDBSyntax by DynamoDbProvider {

    companion object : DynamoTableNameSyntax, DynamoCreateTableSyntax, DynamoDBSyntax by DynamoDbProvider {
        override val tableName = "TRIBE"
        override val createTableParams = json(
            "TableName" to tableName,
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

        suspend fun dynamoTribeRepository() = DynamoTribeRepository().also { ensureTableExists() }
    }

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
