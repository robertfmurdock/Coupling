package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PartyId

data class CouplingConnection(val connectionId: String, val partyId: PartyId, val userPlayer: Player)
