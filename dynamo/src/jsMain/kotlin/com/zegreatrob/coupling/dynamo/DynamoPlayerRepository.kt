package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoPlayerRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    PlayerRepository,
    PlayerListGetByEmail,
    UserEmailSyntax,
    DynamoPlayerJsonMapping {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPlayerRepository>, DynamoDBSyntax by DynamoDbProvider,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoQuerySyntax,
        DynamoItemDeleteSyntax {
        override val construct = ::DynamoPlayerRepository
        override val tableName: String = "PLAYER"
        val playerEmailIndex = "PlayerEmailIndex"

        override val createTableParams: Json
            get() = json(
                "TableName" to tableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "KeyType" to "HASH"
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "KeyType" to "RANGE"
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "id",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "email",
                        "AttributeType" to "S"
                    )
                ),
                "BillingMode" to "PAY_PER_REQUEST"
            ).add(
                json(
                    "GlobalSecondaryIndexes" to arrayOf(
                        json(
                            "IndexName" to playerEmailIndex,
                            "KeySchema" to arrayOf(
                                json(
                                    "AttributeName" to "email",
                                    "KeyType" to "HASH"
                                ),
                                json(
                                    "AttributeName" to "id",
                                    "KeyType" to "range"
                                )
                            ),
                            "Projection" to json(
                                "NonKeyAttributes" to arrayOf(
                                    "tribeId",
                                    "timestamp"
                                ),
                                "ProjectionType" to "INCLUDE"
                            )
                        )
                    )
                )
            )
    }

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.scanForItemList().map { it.toPlayerRecord() }

    private fun Json.toPlayerRecord(): Record<TribeElement<Player>> {
        val player = toPlayer()
        return toRecord(tribeId().with(player))
    }

    private fun Json.tribeId() = TribeId(this["tribeId"].unsafeCast<String>())

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = performPutItem(tribeIdPlayer.toDynamoJson())

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performDelete(
        playerId, recordJson(), tribeId
    )

    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> = tribeId.scanForDeletedItemList()
        .map { it.toPlayerRecord() }

    override suspend fun getPlayerIdsByEmail(email: String): List<TribeElement<String>> {
        val playerIdsWithEmail = performQuery(emailQueryParams(email))
            .itemsNode()
            .map { it["id"].unsafeCast<String>() }

        return performScan(playerIdScanParams(playerIdsWithEmail))
            .itemsNode()
            .sortByRecordTimestamp()
            .groupBy { it.getDynamoStringValue("id") }
            .map { it.value.last() }
            .filter { it["email"] == email }
            .map {
                TribeId(it.getDynamoStringValue("tribeId")!!)
                    .with(it.getDynamoStringValue("id")!!)
            }
    }

    private fun playerIdScanParams(recordTribePlayerIds: List<String>) = json(
        "TableName" to tableName,
        "IndexName" to playerEmailIndex,
        "ExpressionAttributeValues" to json(
            ":playerIdList" to recordTribePlayerIds.toTypedArray()
        ),
        "FilterExpression" to "contains(:playerIdList, id)"
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to tableName,
        "IndexName" to playerEmailIndex,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email"
    )

}
