package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository

class MemoryPlayerRepository : PlayerRepository, TypeRecordSyntax<TribeIdPlayer>, RecordSaveSyntax<TribeIdPlayer> {

    override var records = emptyList<Record<TribeIdPlayer>>()

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.record().save()

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.players()
        .filterNot { it.isDeleted }
        .map { it.data.player }

    private fun TribeId.players() = records.asSequence()
        .filter { (data) -> data.tribeId == this }
        .groupBy { (data) -> data.player.id }
        .map { it.value.last() }

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean {
        val tribeIdPlayer = tribeId.players().find { (data) -> data.player.id == playerId }?.data

        return if (tribeIdPlayer == null) {
            false
        } else {
            tribeIdPlayer.deletionRecord().save()
            true
        }
    }

    override suspend fun getDeleted(tribeId: TribeId) = tribeId.players()
        .filter { it.isDeleted }
        .map { it.data.player }

}
