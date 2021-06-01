package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoLiveInfoRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    LiveInfoRepository, DynamoPlayerJsonMapping {

    override suspend fun get(tribeId: TribeId) = performQuery(queryParams(tribeId))
        .itemsNode()
        .firstOrNull()
        ?.toLiveInfo()
        ?: LiveInfo(emptyList())

    override suspend fun save(tribeId: TribeId, info: LiveInfo) = performPutItem(
        TribeElement(tribeId, info).toDynamoJson()
    )

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
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "tribeId",
                        "AttributeType" to "S"
                    )
                ),
                "BillingMode" to "PAY_PER_REQUEST"
            )
    }

    private fun Json.toLiveInfo() = LiveInfo(
        connections = this["connections"].unsafeCast<Array<Json>>()
            .mapNotNull {
                it["userPlayer"].unsafeCast<Json>().toPlayer()
                    ?.let { player -> CouplingConnection(it["connectionId"].toString(), player) }
            }
    )

    private fun TribeElement<LiveInfo>.toDynamoJson() = json(
        "tribeId" to id.value,
        "connections" to element.connections.map {
            json(
                "connectionId" to it.connectionId,
                "userPlayer" to it.userPlayer.toDynamoJson()
            )
        }.toTypedArray()
    )

}
