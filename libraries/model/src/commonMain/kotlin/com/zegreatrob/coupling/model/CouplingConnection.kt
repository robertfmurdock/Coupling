package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player

data class CouplingConnection(val connectionId: String, val partyId: PartyId, val userPlayer: Player)
