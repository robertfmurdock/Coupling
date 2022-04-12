package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.PartyElement
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoPlayerRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    PlayerEmailRepository,
    UserIdSyntax,
    DynamoPlayerJsonMapping,
    RecordSyntax {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPlayerRepository>, DynamoDBSyntax by DynamoDbProvider,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoQuerySyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoLoggingSyntax {
        override val construct = ::DynamoPlayerRepository
        override val tableName: String = "PLAYER"
        const val playerEmailIndex = "PlayerEmailIndex"

        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "KeyType" to "HASH"
                    ),
                    json(
                        "AttributeName" to "timestamp+id",
                        "KeyType" to "RANGE"
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "timestamp+id",
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
                                    "KeyType" to "RANGE"
                                )
                            ),
                            "Projection" to json(
                                "NonKeyAttributes" to arrayOf(
                                    "tribeId",
                                    "timestamp",
                                    "isDeleted"
                                ),
                                "ProjectionType" to "INCLUDE"
                            )
                        )
                    )
                )
            )
    }

    override suspend fun getPlayers(partyId: PartyId) = partyId.queryForItemList().mapNotNull { it.toPlayerRecord() }

    suspend fun getPlayerRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        performQuery(partyId.itemListQueryParams()).itemsNode()
    }.mapNotNull { it.toPlayerRecord() }

    private fun Json.toPlayerRecord() = toPlayer()?.let { toRecord(PartyId().with(it)) }

    private fun Json.PartyId() = PartyId(this["tribeId"].unsafeCast<String>())

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = saveRawRecord(
        tribeIdPlayer.copyWithIdCorrection().toRecord()
    )

    private fun TribeIdPlayer.copyWithIdCorrection() = copy(element = with(element) {
        copy(id = id)
    })

    suspend fun saveRawRecord(record: TribeRecord<Player>) = performPutItem(record.asDynamoJson())

    override suspend fun deletePlayer(partyId: PartyId, playerId: String) = performDelete(
        playerId, partyId, now(), { toPlayerRecord() }, { asDynamoJson() }
    )

    override suspend fun getDeleted(partyId: PartyId): List<Record<TribeIdPlayer>> = partyId.queryForDeletedItemList()
        .mapNotNull { it.toPlayerRecord() }

    override suspend fun getPlayerIdsByEmail(email: String): List<PartyElement<String>> =
        logAsync("getPlayerIdsByEmail") {
            val playerIdsWithEmail = logAsync("playerIdsWithEmail") {
                performQuery(emailQueryParams(email))
                    .itemsNode()
                    .mapNotNull { it.getDynamoStringValue("id") }
                    .toSet()
            }
            logAsync("recordsWithIds") {
                scanAllRecords(playerIdScanParams(playerIdsWithEmail))
                    .sortByRecordTimestamp()
                    .groupBy { it.getDynamoStringValue("id") }
                    .map { it.value.last() }
                    .filter { it["email"] == email && it["isDeleted"] != true }
                    .map {
                        PartyId(it.getDynamoStringValue("tribeId") ?: "")
                            .with(it.getDynamoStringValue("id") ?: "")
                    }
            }
        }

    private fun playerIdScanParams(recordTribePlayerIds: Set<String>) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":playerIdList" to recordTribePlayerIds.toTypedArray()
        ),
        "FilterExpression" to "contains(:playerIdList, id)"
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to prefixedTableName,
        "IndexName" to playerEmailIndex,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email"
    )

}
