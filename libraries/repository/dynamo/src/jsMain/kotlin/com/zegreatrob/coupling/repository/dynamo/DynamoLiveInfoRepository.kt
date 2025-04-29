package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

class DynamoLiveInfoRepository private constructor(override val userId: UserId, override val clock: Clock) :
    LiveInfoRepository,
    DynamoPlayerJsonMapping {

    override suspend fun connectionList(partyId: PartyId) = partyId.logAsync("connectionList") {
        queryAllRecords(queryParams(partyId))
            .mapNotNull {
                it["userPlayer"].unsafeCast<Json>().toPlayer()
                    ?.let { player -> CouplingConnection(it["id"].toString(), partyId, player) }
            }.sortedBy { it.connectionId }
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    override suspend fun get(connectionId: String) = connectionId.logAsync("getConnection") {
        queryAllRecords(queryParams(connectionId)).firstNotNullOfOrNull {
            it["userPlayer"].unsafeCast<Json>().toPlayer()
                ?.let { player ->
                    CouplingConnection(
                        it["id"].toString(),
                        it["tribeId"].toString().let(::PartyId),
                        player,
                    )
                }
        }
    }

    override suspend fun save(connection: CouplingConnection) = connection.connectionId.logAsync("saveConnection") {
        performPutItem(
            connection.toDynamoJson(),
        )
    }

    override suspend fun deleteIt(partyId: PartyId, connectionId: String) = connectionId.logAsync("deleteConnection") {
        performDeleteItem(
            json(
                "entityType" to ENTITY_TYPE,
                "tribeId+id" to "${partyId.value}+$connectionId",
            ),
        )
    }

    private fun queryParams(partyId: PartyId) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":entityType" to ENTITY_TYPE,
            ":tribeId" to partyId.value.toString(),
        ),
        "ExpressionAttributeNames" to json(
            "#sortKey" to "tribeId+id",
        ),
        "KeyConditionExpression" to "entityType = :entityType and begins_with(#sortKey, :tribeId)",
    )

    private fun queryParams(connectionId: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(
            ":entityType" to "USER_CONNECTION",
            ":id" to connectionId,
        ),
        "KeyConditionExpression" to "entityType = :entityType",
        "FilterExpression" to "id = :id",
    )

    companion object :
        DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoItemDeleteSyntax,
        DynamoScanSyntax {
        override val tableName = "LIVE_CONNECTION"
        const val ENTITY_TYPE = "USER_CONNECTION"

        private val ensure by lazy {
            MainScope().async { ensureTableExists() }
        }

        suspend operator fun invoke(userId: UserId, clock: Clock) = DynamoLiveInfoRepository(userId, clock)
            .also { ensure.await() }

        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "entityType",
                        "KeyType" to "HASH",
                    ),
                    json(
                        "AttributeName" to "tribeId+id",
                        "KeyType" to "RANGE",
                    ),
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "entityType",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "tribeId+id",
                        "AttributeType" to "S",
                    ),
                ),
                "BillingMode" to "PAY_PER_REQUEST",
            )
    }

    private fun CouplingConnection.toDynamoJson() = json(
        "entityType" to ENTITY_TYPE,
        "tribeId" to partyId.value.toString(),
        "id" to connectionId,
        "tribeId+id" to "${partyId.value}+$connectionId",
        "userPlayer" to userPlayer.toDynamoJson(),
    )
}
