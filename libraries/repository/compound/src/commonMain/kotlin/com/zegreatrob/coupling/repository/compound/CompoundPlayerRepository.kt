package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class CompoundPlayerRepository(
    private val repository1: PlayerEmailRepository,
    private val repository2: PlayerEmailRepository,
) : PlayerEmailRepository by repository1 {

    override suspend fun save(partyPlayer: PartyElement<Player>) = arrayOf(repository1, repository2).forEach {
        it.save(partyPlayer)
    }

    override suspend fun deletePlayer(partyId: PartyId, playerId: PlayerId) = repository1.deletePlayer(partyId, playerId)
        .also { repository2.deletePlayer(partyId, playerId) }
}
