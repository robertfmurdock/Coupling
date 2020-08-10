package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : PlayerContext> C1.attachPlayer(player: Player, tribe: Tribe, sdk: Sdk) = also {
    this.player = player
    this.tribe = tribe
    this.sdk = sdk
}

fun <C1 : PlayerContext> C1.attachPlayer(): suspend (Triple<Player, Tribe, Sdk>) -> C1 = { triple ->
    also {
        player = triple.first
        tribe = triple.second
        sdk = triple.third
    }
}

abstract class PlayerContext : TribeContext() {
    lateinit var player: Player
}
