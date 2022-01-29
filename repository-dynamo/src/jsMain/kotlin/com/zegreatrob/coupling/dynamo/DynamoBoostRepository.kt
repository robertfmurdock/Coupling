package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.ExtendedBoostRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
            ?.let { if (it.isDeleted) null else it }
    }

    override suspend fun save(boost: Boost) {
        val boostRecord = boost.toRecord()
        val dynamoJson = boostRecord.asDynamoJson()
        coroutineScope {
            performPutItem(dynamoJson)
            boost.tribeIds.forEach { tribeId ->
                launch { performPutItem(dynamoJson.add(json("pk" to tribeKey(tribeId)))) }
            }
        }
    }

    override suspend fun delete() {
        get()
            ?.copy(isDeleted = true)
            ?.asDynamoJson()
            ?.let {
                performPutItem(it)
            }

    }

    override suspend fun getByTribeId(tribeId: TribeId) = getByPk(tribeKey(tribeId))
        ?.verifyTribeHasCurrentAccess(tribeId)

    private fun tribeKey(tribeId: TribeId) = "tribe-${tribeId.value}"

    private suspend fun Record<Boost>.verifyTribeHasCurrentAccess(tribeId: TribeId) = getByPk(userKey(data.userId))
        ?.let { if (it.data.tribeIds.contains(tribeId)) it else null }

    private fun userKey(userId: String) = "user-$userId"

    private fun queryParams(id: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":pk" to id),
        "KeyConditionExpression" to "pk = :pk"
    )

}
