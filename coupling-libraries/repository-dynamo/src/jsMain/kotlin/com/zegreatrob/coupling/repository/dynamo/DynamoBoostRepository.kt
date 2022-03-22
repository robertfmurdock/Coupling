package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import kotlinext.js.clone
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.json

class DynamoBoostRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    ExtendedBoostRepository,
    DynamoBoostJsonMapping,
    RecordSyntax {

    companion object : DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoScanSyntax,
        DynamoLoggingSyntax {
        override val tableName = "BOOST"
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoBoostRepository(userId, clock)
            .also { ensureTableExists() }

        override val createTableParams = json(
            "TableName" to prefixedTableName,
            "KeySchema" to arrayOf(
                json(
                    "AttributeName" to "pk",
                    "KeyType" to "HASH"
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "KeyType" to "RANGE"
                )
            ),
            "AttributeDefinitions" to arrayOf(
                json(
                    "AttributeName" to "pk",
                    "AttributeType" to "S"
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "AttributeType" to "S"
                )
            ),
            "BillingMode" to "PAY_PER_REQUEST"
        )
    }

    override suspend fun get(): Record<Boost>? = getByPk(userKey(userId))

    private suspend fun getByPk(pk: String) = logAsync("get boost for pk") {
        performQuery(queryParams(pk))
            .itemsNode()
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

    private fun buildTribeCopyRecordJson(tribeIdsToUpdate: Set<TribeId>, dynamoJson: Json) =
        tribeIdsToUpdate.map { tribeId ->
            copyWithDifferentPk(dynamoJson, tribeId)
        }

    private suspend fun List<Json>.performPutItems() = coroutineScope {
        forEach {
            launch { performPutItem(it) }
        }
    }

    private fun copyWithDifferentPk(dynamoJson: Json, tribeId: TribeId) =
        clone(dynamoJson).add(json("pk" to tribeKey(tribeId)))

    private suspend fun allTribeCopiesToUpdate(boostRecord: Record<Boost>): Set<TribeId> {
        val previousRecord = getByPk(userKey(boostRecord.data.userId))
        return boostRecord.data.tribeIds + previousRecord.previousRecordTribeIds()
    }

    private fun Record<Boost>?.previousRecordTribeIds() = (this?.data?.tribeIds ?: emptySet())

    override suspend fun delete() {
        get()
            ?.copy(isDeleted = true)
            ?.putRecordWithClones()
    }

    override suspend fun getByTribeId(tribeId: TribeId) = getByPk(tribeKey(tribeId))
        ?.takeIf { it.data.tribeIds.contains(tribeId) }

    private fun tribeKey(tribeId: TribeId) = "tribe-${tribeId.value}"

    private fun userKey(userId: String) = "user-$userId"

    private fun queryParams(id: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":pk" to id),
        "KeyConditionExpression" to "pk = :pk"
    )

}
