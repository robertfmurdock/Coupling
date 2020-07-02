package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : FullTribeContext> C1.attach(players: List<Player>, pins: List<Pin>, tribe: Tribe, sdk: Sdk) = also {
    this.players = players
    this.tribe = tribe
    this.pins = pins
    this.sdk = sdk
}

data class FullTribeData(val players: List<Player>, val pins: List<Pin>, val tribe: Tribe, val sdk: Sdk)

fun <C1 : FullTribeContext> C1.attach(): suspend (FullTribeData) -> C1 = { data: FullTribeData ->
    also {
        players = data.players
        tribe = data.tribe
        pins = data.pins
        sdk = data.sdk
    }
}

open class FullTribeContext : TribeContext() {
    lateinit var players: List<Player>
    lateinit var pins: List<Pin>
}