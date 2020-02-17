package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository

class MemoryPlayerRepository : PlayerRepository, TypeRecordSyntax<TribeIdPlayer> {

    private var playerMap = emptyList<Record<TribeIdPlayer>>()

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        playerMap = playerMap + tribeIdPlayer.record()
    }

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.players()
        .filterNot { it.isDeleted }
        .map { it.data.player }

    private fun TribeId.players() = playerMap.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.player.id }
        .map { it.value.last() }

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean {
        val tribeIdPlayer = tribeId.players().find { (data) -> data.player.id == playerId }?.data

        return if (tribeIdPlayer == null) {
            false
        } else {
            playerMap = playerMap + tribeIdPlayer.deleteRecord()
            true
        }
    }

    override suspend fun getDeleted(tribeId: TribeId) = tribeId.players()
        .filter { it.isDeleted }
        .map { it.data.player }

}
