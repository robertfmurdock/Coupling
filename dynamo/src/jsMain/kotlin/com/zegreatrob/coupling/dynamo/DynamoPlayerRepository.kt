package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository

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

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.scanForItemList().map {
        val player = it.toPlayer()
        it.toRecord(tribeId.with(player))
    }

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = performPutItem(tribeIdPlayer.toDynamoJson())

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performDelete(
        playerId, recordJson(), tribeId
    )

    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> = tribeId.scanForDeletedItemList()
        .map { it.toRecord(tribeId.with(it.toPlayer())) }

    override suspend fun getPlayersByEmail(email: String): List<TribeIdPlayer> {
        TODO()
    }

}
