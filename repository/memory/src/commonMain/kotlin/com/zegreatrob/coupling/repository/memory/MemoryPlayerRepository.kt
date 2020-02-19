package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class MemoryPlayerRepository(override val userEmail: String, override val clock: TimeProvider) : PlayerEmailRepository,
    TypeRecordSyntax<TribeIdPlayer>, RecordSaveSyntax<TribeIdPlayer> {

    override var records = emptyList<Record<TribeIdPlayer>>()

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = tribeIdPlayer.record().save()

    override suspend fun getPlayers(tribeId: TribeId) = tribeId.players()
        .filterNot { it.isDeleted }

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

    override suspend fun getDeleted(tribeId: TribeId): List<Record<TribeIdPlayer>> = tribeId.players()
        .filter { it.isDeleted }

    override suspend fun getPlayersByEmail(email: String): List<TribeIdPlayer> = records.asSequence()
        .groupBy { it.data.player.id }
        .map { it.value.last() }
        .filter { it.data.element.email == email }
        .map { it.data }

}
