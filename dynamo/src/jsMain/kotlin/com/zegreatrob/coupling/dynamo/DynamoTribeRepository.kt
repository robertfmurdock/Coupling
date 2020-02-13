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

        suspend operator fun invoke() = DynamoTribeRepository().also { ensureTableExists() }
    }

    override suspend fun getTribe(tribeId: TribeId) = performGetSingleItemQuery(tribeId.value)?.toTribe()

    override suspend fun getTribes() = scanForItemList(scanParams()).map { it.toTribe() }

    private fun scanParams() = json("TableName" to tableName)

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
        "id" to id.value,
        "timestamp" to DateTime.now().isoWithMillis(),
        "name" to name,
        "email" to email,
        "pairingRule" to PairingRule.toValue(pairingRule),
        "defaultBadgeName" to defaultBadgeName,
        "alternateBadgeName" to alternateBadgeName,
        "badgesEnabled" to badgesEnabled,
        "callSignsEnabled" to callSignsEnabled,
        "animationEnabled" to animationEnabled,
        "animationSpeed" to animationSpeed
    )

}
