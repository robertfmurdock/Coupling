package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoPlayerRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    PlayerRepository,
    PlayerListGetByEmail,
    UserEmailSyntax,
    DynamoPlayerJsonMapping {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPlayerRepository>, DynamoDBSyntax by DynamoDbProvider,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoItemDeleteSyntax {
        override val construct = ::DynamoPlayerRepository
        override val tableName: String = "PLAYER"
    }

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.scanForItemList().map { it.toPlayerRecord() }

    private fun Json.toPlayerRecord(): Record<TribeElement<Player>> {
        val player = toPlayer()
        return toRecord(tribeId().with(player))
    }

    private fun Json.tribeId() = TribeId(this["tribeId"].unsafeCast<String>())

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = performPutItem(tribeIdPlayer.toDynamoJson())

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performDelete(
        playerId, recordJson(), tribeId
    )

    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> = tribeId.scanForDeletedItemList()
        .map { it.toPlayerRecord() }

    override suspend fun getPlayersByEmail(email: String): List<TribeElement<Player>> {
        val recordsWithEmail = scanForItemList(emailScanParams(email))
            .map { it.toPlayerRecord() }
            .map { it.data }

        val recordTribePlayerIds = recordsWithEmail.map { it.player.id!! }

        return scanForItemList(playerIdScanParams(recordTribePlayerIds))
            .map { it.toPlayerRecord() }
            .map { it.data }
            .filter { it.element.email == email }
    }

    private fun playerIdScanParams(recordTribePlayerIds: List<String>) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(
            ":playerIdList" to recordTribePlayerIds.toTypedArray()
        ),
        "FilterExpression" to "contains(:playerIdList, id)"
    )

    private fun emailScanParams(email: String) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":email" to email),
        "FilterExpression" to "email = :email"
    )

}
