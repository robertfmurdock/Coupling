package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotools.types.text.NotBlankString
import kotlin.time.Clock

class MemoryPlayerRepository(
    override val userId: UserId,
    override val clock: Clock,
    private val recordBackend: RecordBackend<PartyElement<Player>> = SimpleRecordBackend(),
) : PlayerEmailRepository,
    TypeRecordSyntax<PartyElement<Player>>,
    RecordBackend<PartyElement<Player>> by recordBackend {

    override suspend fun save(partyPlayer: PartyElement<Player>) {
        partyPlayer.copy(element = with(partyPlayer.element) { copy(id = id) })
            .record().save()
    }

    override suspend fun getPlayers(partyId: PartyId) = partyId.players()
        .filterNot { it.isDeleted }

    private fun PartyId.players() = records.asSequence()
        .filter { (data) -> data.partyId == this }
        .groupBy { (data) -> data.player.id }
        .map { it.value.last() }

    override suspend fun deletePlayer(partyId: PartyId, playerId: PlayerId): Boolean {
        val partyIdPlayer = partyId.players().find { (data) -> data.player.id == playerId }?.data

        return if (partyIdPlayer == null) {
            false
        } else {
            partyIdPlayer.deletionRecord().save()
            true
        }
    }

    override suspend fun getDeleted(partyId: PartyId): List<Record<PartyElement<Player>>> = partyId.players()
        .filter { it.isDeleted }

    override suspend fun getPlayerIdsByEmail(email: NotBlankString) = records
        .groupBy { it.data.player.id }
        .map { it.value.last() }
        .filterNot(Record<*>::isDeleted)
        .filter { it.data.element.matches(email.toString()) }
        .map { it.data.partyId.with(it.data.player.id) }
}
