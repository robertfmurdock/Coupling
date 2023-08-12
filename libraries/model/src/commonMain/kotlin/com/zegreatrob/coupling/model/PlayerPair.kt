package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.player.Player

data class PlayerPair(
    val players: List<PartyRecord<Player>>? = null,
    val count: Int? = null,
    val spinsSinceLastPaired: Int? = null,
)
