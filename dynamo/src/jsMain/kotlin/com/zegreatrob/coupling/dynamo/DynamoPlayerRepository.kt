package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoPlayerRepository private constructor() : PlayerRepository {

    companion object : DynamoTableNameSyntax, DynamoCreateTableSyntax, DynamoDBSyntax by DynamoDbProvider,
        DynamoItemPutSyntax,
        DynamoItemListGetSyntax,
        DynamoItemDeleteSyntax,
        DynamoDatatypeSyntax {

        suspend operator fun invoke() = DynamoPlayerRepository().also {
            ensureTableExists()
        }

        override val tableName: String = "PLAYER"
        override val createTableParams = json(
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

    override suspend fun getPlayers(tribeId: TribeId) = scanForItemList(tribeId.scanParams())
        .map { it.toPlayer() }

    private fun TribeId.scanParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":tribeId" to value.dynamoString()),
        "FilterExpression" to "tribeId = :tribeId"
    )

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = performPutItem(tribeIdPlayer.toDynamoJson())

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performDelete(playerId, tribeId)

    override suspend fun getDeleted(tribeId: TribeId): List<Player> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun TribeIdPlayer.toDynamoJson() = json(
        "tribeId" to tribeId.value.dynamoString(),
        "id" to player.id?.dynamoString(),
        "timestamp" to DateTime.now().isoWithMillis().dynamoString(),
        "name" to player.name.dynamoString(),
        "email" to player.email.dynamoString(),
        "badge" to player.badge?.dynamoNumber(),
        "callSignAdjective" to player.callSignAdjective.dynamoString(),
        "callSignNoun" to player.callSignNoun.dynamoString(),
        "imageURL" to player.imageURL.dynamoString()
    )

    private fun Json.toPlayer() = Player(
        id = getDynamoStringValue("id")!!,
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        badge = getDynamoNumberValue("badge")?.toInt(),
        callSignAdjective = getDynamoStringValue("callSignAdjective"),
        callSignNoun = getDynamoStringValue("callSignNoun"),
        imageURL = getDynamoStringValue("imageURL")
    )

}

