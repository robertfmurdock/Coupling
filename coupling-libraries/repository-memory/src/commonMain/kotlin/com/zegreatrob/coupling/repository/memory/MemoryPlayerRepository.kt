package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.partyId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class MemoryPlayerRepository(
    override val userId: String,
    override val clock: TimeProvider,
    private val recordBackend: RecordBackend<PartyElement<Player>> = SimpleRecordBackend()
) : PlayerEmailRepository,
    TypeRecordSyntax<PartyElement<Player>>, RecordBackend<PartyElement<Player>> by recordBackend {

    override suspend fun save(tribeIdPlayer: PartyElement<Player>) {
        tribeIdPlayer.copy(element = with(tribeIdPlayer.element) { copy(id = id) })
            .record().save()
    }

    override suspend fun getPlayers(tribeId: PartyId) = tribeId.players()
        .filterNot { it.isDeleted }

    private fun PartyId.players() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.player.id }
        .map { it.value.last() }

    override suspend fun deletePlayer(tribeId: PartyId, playerId: String): Boolean {
        val tribeIdPlayer = tribeId.players().find { (data) -> data.player.id == playerId }?.data

        return if (tribeIdPlayer == null) {
            false
        } else {
            tribeIdPlayer.deletionRecord().save()
            true
        }
    }

    override suspend fun getDeleted(tribeId: PartyId): List<Record<PartyElement<Player>>> = tribeId.players()
        .filter { it.isDeleted }

    override suspend fun getPlayerIdsByEmail(email: String) = records
        .asSequence()
        .groupBy { it.data.player.id }
        .map { it.value.last() }
        .filterNot { it.isDeleted }
        .filter { it.data.element.email == email }
        .map { it.data.id.with(it.data.player.id) }
        .toList()

}
