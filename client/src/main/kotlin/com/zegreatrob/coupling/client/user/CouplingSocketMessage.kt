package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.model.player.Player

data class CouplingSocketMessage(var text: String, var players: List<Player>)
