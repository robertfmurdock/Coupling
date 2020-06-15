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

open class FullTribeContext : TribeContext() {
    lateinit var players: List<Player>
    lateinit var pins: List<Pin>
}