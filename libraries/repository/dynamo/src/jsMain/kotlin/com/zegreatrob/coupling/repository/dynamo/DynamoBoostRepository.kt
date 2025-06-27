package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import js.objects.Object.Companion.assign
import js.objects.unsafeJso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoBoostRepository private constructor(override val userId: UserId, override val clock: Clock) :
    ExtendedBoostRepository,
    DynamoBoostJsonMapping,
    RecordSyntax {

    companion object :
        DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoScanSyntax,
        DynamoLoggingSyntax {
        override val tableName = "BOOST"

        private val ensure by lazy {
            MainScope().async { ensureTableExists() }
        }

        suspend operator fun invoke(userId: UserId, clock: Clock) = DynamoBoostRepository(userId, clock)
            .also { ensure.await() }

        override val createTableParams = json(
            "TableName" to prefixedTableName,
            "KeySchema" to arrayOf(
                json(
                    "AttributeName" to "pk",
                    "KeyType" to "HASH",
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "KeyType" to "RANGE",
                ),
            ),
            "AttributeDefinitions" to arrayOf(
                json(
                    "AttributeName" to "pk",
                    "AttributeType" to "S",
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "AttributeType" to "S",
                ),
            ),
            "BillingMode" to "PAY_PER_REQUEST",
        )
    }

    override suspend fun get(): Record<Boost>? = getByPk(userKey(userId))

    private suspend fun getByPk(pk: String) = logAsync("get boost for pk") {
        queryAllRecords(queryParams(pk))
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.toBoostRecord()
            ?.takeUnless { it.isDeleted }
    }

    override suspend fun save(boost: Boost) = boost.toRecord().putRecordWithClones()

    private suspend fun Record<Boost>.putRecordWithClones() {
        val dynamoJson = asDynamoJson()
        val tribeIdsToUpdate = allTribeCopiesToUpdate(this)
        (buildTribeCopyRecordJson(tribeIdsToUpdate, dynamoJson) + dynamoJson)
            .performPutItems()
    }

    private fun buildTribeCopyRecordJson(partyIdsToUpdate: Set<PartyId>, dynamoJson: Json) = partyIdsToUpdate.map { tribeId ->
        copyWithDifferentPk(dynamoJson, tribeId)
    }

    private suspend fun List<Json>.performPutItems() = coroutineScope {
        forEach {
            launch { performPutItem(it) }
        }
    }

    private fun copyWithDifferentPk(dynamoJson: Json, partyId: PartyId) = assign(unsafeJso(), dynamoJson).add(json("pk" to tribeKey(partyId)))

    private suspend fun allTribeCopiesToUpdate(boostRecord: Record<Boost>): Set<PartyId> {
        val previousRecord = getByPk(userKey(boostRecord.data.userId))
        return boostRecord.data.partyIds + previousRecord.previousRecordTribeIds()
    }

    private fun Record<Boost>?.previousRecordTribeIds() = (this?.data?.partyIds ?: emptySet())

    override suspend fun deleteIt() {
        get()
            ?.copy(isDeleted = true)
            ?.putRecordWithClones()
    }

    override suspend fun getByPartyId(partyId: PartyId) = getByPk(tribeKey(partyId))
        ?.takeIf { it.data.partyIds.contains(partyId) }

    private fun tribeKey(partyId: PartyId) = "tribe-${partyId.value}"

    private fun userKey(userId: UserId) = "user-${userId.value}"

    private fun queryParams(id: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":pk" to id),
        "KeyConditionExpression" to "pk = :pk",
    )
}
