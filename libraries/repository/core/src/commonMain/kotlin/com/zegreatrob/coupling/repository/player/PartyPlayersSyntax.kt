package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface PartyPlayersSyntax : PartyIdLoadPlayersSyntax {
    suspend fun PartyId.getPlayerList() = loadPlayers().elements
}
