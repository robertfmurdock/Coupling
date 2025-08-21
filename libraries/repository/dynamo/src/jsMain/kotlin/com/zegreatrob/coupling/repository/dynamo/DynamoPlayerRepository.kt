package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoPlayerRepository private constructor(override val userId: UserId, override val clock: Clock) :
    PlayerEmailRepository,
    UserIdProvider,
    DynamoPlayerJsonMapping,
    RecordSyntax {

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoPlayerRepository>(),
        DynamoDBSyntax by DynamoDbProvider,
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoQuerySyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoLoggingSyntax {
        override val construct = ::DynamoPlayerRepository
        override val tableName: String = "PLAYER"
        const val PLAYER_EMAIL_INDEX = "PlayerEmailIndex"
        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "KeyType" to "HASH",
                    ),
                    json(
                        "AttributeName" to "timestamp+id",
                        "KeyType" to "RANGE",
                    ),
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "timestamp+id",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "id",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "email",
                        "AttributeType" to "S",
                    ),
                ),
                "BillingMode" to "PAY_PER_REQUEST",
            ).add(
                json(
                    "GlobalSecondaryIndexes" to arrayOf(
                        json(
                            "IndexName" to PLAYER_EMAIL_INDEX,
                            "KeySchema" to arrayOf(
                                json(
                                    "AttributeName" to "email",
                                    "KeyType" to "HASH",
                                ),
                                json(
                                    "AttributeName" to "id",
                                    "KeyType" to "RANGE",
                                ),
                            ),
                            "Projection" to json(
                                "NonKeyAttributes" to arrayOf(
                                    "tribeId",
                                    "timestamp",
                                    "isDeleted",
                                ),
                                "ProjectionType" to "INCLUDE",
                            ),
                        ),
                    ),
                ),
            )
    }

    override suspend fun getPlayers(partyId: PartyId) = partyId.queryForItemList().mapNotNull { it.toPlayerRecord() }

    suspend fun getPlayerRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        queryAllRecords(partyId.itemListQueryParams())
    }.mapNotNull { it.toPlayerRecord() }

    private fun Json.toPlayerRecord() = toPlayer()?.let { toRecord(PartyId().with(it)) }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun Json.PartyId() = PartyId((this["tribeId"].unsafeCast<String>()))

    override suspend fun save(partyPlayer: PartyElement<Player>) = saveRawRecord(
        partyPlayer.copyWithIdCorrection().toRecord(),
    )

    private fun PartyElement<Player>.copyWithIdCorrection() = copy(
        element = with(element) {
            copy(id = id)
        },
    )

    suspend fun saveRawRecord(record: PartyRecord<Player>) = performPutItem(record.asDynamoJson())

    override suspend fun deletePlayer(partyId: PartyId, playerId: PlayerId) = performDelete(
        playerId.value.toString(),
        partyId,
        now(),
        { toPlayerRecord() },
        { asDynamoJson() },
    )

    override suspend fun getDeleted(partyId: PartyId): List<PartyRecord<Player>> = partyId.queryForDeletedItemList()
        .mapNotNull { it.toPlayerRecord() }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    override suspend fun getPlayersByEmail(emails: List<NotBlankString>): List<PartyRecord<Player>> = logAsync("getPlayerIdsByEmail") {
        val playersWithEmail = getPlayerIdsWithEmail(emails)
        logAsync("recordsWithIds") {
            scanAllRecords(playerIdScanParams(playersWithEmail))
                .sortByRecordTimestamp()
                .groupBy { it.getDynamoStringValue("id") }
                .map { it.value.last() }
                .filter { it["isDeleted"] != true }
                .filter { playerHasEmailAndIsNotDeleted(emails, it) }
                .mapNotNull { it.toPlayerRecord() }
        }
    }

    private fun playerHasEmailAndIsNotDeleted(
        emails: List<NotBlankString>,
        json: Json,
    ): Boolean {
        val playerEmails = listOf(json["email"]) + json["unvalidatedEmails"].unsafeCast<Array<*>>()
        return emails.any { email -> playerEmails.contains(email.toString()) }
    }

    private suspend fun getPlayerIdsWithEmail(emails: List<NotBlankString>): Set<String> = coroutineScope {
        val exactMatches = async { playerIdsWithEmail(emails) }
        val additionalMatches = async { playerIdsWithAdditionalEmail(emails) }

        exactMatches.await() + additionalMatches.await()
    }

    private suspend fun CoroutineScope.playerIdsWithAdditionalEmail(emails: List<NotBlankString>): List<String> = logAsync("playerIdsWithAdditionalEmails") {
        emails.map { email ->
            async {
                scanAllRecords(
                    json(
                        "TableName" to prefixedTableName,
                        "ExpressionAttributeValues" to json(":email" to email.toString()),
                        "FilterExpression" to "contains (unvalidatedEmails, :email)",
                    ),
                )
                    .mapNotNull { it.getDynamoStringValue("id") }
                    .toSet()
            }
        }.awaitAll()
            .flatten()
    }

    private suspend fun CoroutineScope.playerIdsWithEmail(emails: List<NotBlankString>): Set<String> = logAsync("playerIdsWithEmail") {
        emails.map { email ->
            async {
                queryAllRecords(emailQueryParams(email.toString()))
                    .mapNotNull { it.getDynamoStringValue("id") }
            }
        }.awaitAll()
            .flatten()
            .toSet()
    }

    private fun playerIdScanParams(recordTribePlayerIds: Set<String>) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":playerIdList" to recordTribePlayerIds.toTypedArray(),
        ),
        "FilterExpression" to "contains(:playerIdList, id)",
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to prefixedTableName,
        "IndexName" to PLAYER_EMAIL_INDEX,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email",
    )
}
