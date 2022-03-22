package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoLiveInfoRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    LiveInfoRepository, DynamoPlayerJsonMapping {

    override suspend fun connectionList(tribeId: TribeId) = tribeId.logAsync("connectionList") {
        performQuery(queryParams(tribeId))
            .itemsNode()
            .mapNotNull {
                it["userPlayer"].unsafeCast<Json>().toPlayer()
                    ?.let { player -> CouplingConnection(it["id"].toString(), tribeId, player) }
            }.sortedBy { it.connectionId }
    }

    override suspend fun get(connectionId: String) = connectionId.logAsync("getConnection") {
        performQuery(queryParams(connectionId))
            .itemsNode()
            .mapNotNull {
                it["userPlayer"].unsafeCast<Json>().toPlayer()
                    ?.let { player ->
                        CouplingConnection(
                            it["id"].toString(),
                            it["tribeId"].toString().let(::TribeId),
                            player
                        )
                    }
            }.firstOrNull()
    }

    override suspend fun save(connection: CouplingConnection) = connection.connectionId.logAsync("saveConnection") {
        performPutItem(
            connection.toDynamoJson()
        )
    }

    override suspend fun delete(tribeId: TribeId, connectionId: String) = connectionId.logAsync("deleteConnection") {
        performDeleteItem(
            json(
                "entityType" to ENTITY_TYPE,
                "tribeId+id" to "${tribeId.value}+$connectionId"
            )
        )
    }

    private fun queryParams(tribeId: TribeId) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":entityType" to ENTITY_TYPE,
            ":tribeId" to tribeId.value
        ),
        "ExpressionAttributeNames" to json(
            "#sortKey" to "tribeId+id",
        ),
        "KeyConditionExpression" to "entityType = :entityType and begins_with(#sortKey, :tribeId)"
    )

    private fun queryParams(connectionId: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":entityType" to "USER_CONNECTION",
            ":id" to connectionId
        ),
        "KeyConditionExpression" to "entityType = :entityType",
        "FilterExpression" to "id = :id"
    )

    companion object : DynamoDBSyntax by DynamoDbProvider,
        com.zegreatrob.coupling.repository.dynamo.CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoItemDeleteSyntax,
        DynamoScanSyntax {
        override val tableName = "LIVE_CONNECTION"
        const val ENTITY_TYPE = "USER_CONNECTION"
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoLiveInfoRepository(userId, clock)
            .also { ensureTableExists() }

        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "entityType",
                        "KeyType" to "HASH"
                    ),
                    json(
                        "AttributeName" to "tribeId+id",
                        "KeyType" to "RANGE"
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "entityType",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "tribeId+id",
                        "AttributeType" to "S"
                    )
                ),
                "BillingMode" to "PAY_PER_REQUEST"
            )
    }

    private fun CouplingConnection.toDynamoJson() = json(
        "entityType" to ENTITY_TYPE,
        "tribeId" to tribeId.value,
        "id" to connectionId,
        "tribeId+id" to "${tribeId.value}+$connectionId",
        "userPlayer" to userPlayer.toDynamoJson()
    )

}
