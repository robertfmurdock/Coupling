package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : PlayerContext> C1.attachPlayer(player: Player, tribe: Tribe, sdk: Sdk) = also {
    this.player = player
    this.tribe = tribe
    this.sdk = sdk
}

abstract class PlayerContext : TribeContext() {
    lateinit var player: Player
}

fun <C1 : PlayersContext> C1.attachPlayers(players: List<Player>, tribe: Tribe, sdk: Sdk) = also {
    this.players = players
    this.tribe = tribe
    this.sdk = sdk
}

abstract class PlayersContext : TribeContext() {
    lateinit var players: List<Player>
}