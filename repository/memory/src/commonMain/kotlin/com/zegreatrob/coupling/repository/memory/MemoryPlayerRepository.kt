package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class MemoryPlayerRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<TribeIdPlayer> = SimpleRecordBackend()
) : PlayerEmailRepository,
    TypeRecordSyntax<TribeIdPlayer>, RecordBackend<TribeIdPlayer> by recordBackend {

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        tribeIdPlayer.copy(element = with(tribeIdPlayer.element) { copy(id = id ?: "${uuid4()}") })
            .record().save()
    }

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

    override suspend fun getPlayerIdsByEmail(email: String) = records.asSequence()
        .groupBy { it.data.player.id }
        .map { it.value.last() }
        .filterNot { it.isDeleted }
        .filter { it.data.element.email == email }
        .map { it.data.id.with(it.data.player.id!!) }

}
