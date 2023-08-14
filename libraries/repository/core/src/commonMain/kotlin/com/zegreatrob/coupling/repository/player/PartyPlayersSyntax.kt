package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface PartyPlayersSyntax : PartyIdLoadPlayersTrait {
    suspend fun PartyId.getPlayerList() = loadPlayers().elements
}
