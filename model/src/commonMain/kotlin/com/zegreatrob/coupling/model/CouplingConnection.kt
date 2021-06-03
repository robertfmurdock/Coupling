package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId

data class CouplingConnection(val connectionId: String, val tribeId: TribeId, val userPlayer: Player)
