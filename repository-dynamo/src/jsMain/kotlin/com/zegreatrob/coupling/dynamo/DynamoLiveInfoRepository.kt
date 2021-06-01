package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoLiveInfoRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    LiveInfoRepository, DynamoPlayerJsonMapping {

    override suspend fun get(tribeId: TribeId) = performQuery(queryParams(tribeId))
        .itemsNode()
        .mapNotNull {
            it["userPlayer"].unsafeCast<Json>().toPlayer()
                ?.let { player -> CouplingConnection(it["id"].toString(), tribeId, player) }
        }.sortedBy { it.connectionId }

    override suspend fun save(connection: CouplingConnection) = performPutItem(
        connection.toDynamoJson()
    )

    override suspend fun delete(tribeId: TribeId, connectionId: String) {
        performDeleteItem(
            json(
                "id" to connectionId,
                "tribeId" to tribeId.value
            )
        )
    }

    private fun queryParams(tribeId: TribeId) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to tribeId.value),
        "KeyConditionExpression" to "tribeId = :tribeId"
    )

    companion object : DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoItemDeleteSyntax,
        DynamoScanSyntax {
        override val tableName = "LIVE_INFO"
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoLiveInfoRepository(userId, clock)
            .also { ensureTableExists() }

        override val createTableParams: Json
            get() = json(
                "TableName" to tableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "KeyType" to "HASH"
                    ),
                    json(
                        "AttributeName" to "id",
                        "KeyType" to "RANGE"
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "id",
                        "AttributeType" to "S"
                    )
                ),
                "BillingMode" to "PAY_PER_REQUEST"
            )
    }

    private fun CouplingConnection.toDynamoJson() = json(
        "tribeId" to tribeId.value,
        "id" to connectionId,
        "userPlayer" to userPlayer.toDynamoJson()
    )

}
