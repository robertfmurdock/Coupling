package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.Sdk

fun <C1 : PlayerContext> C1.attachPlayer(): suspend (Triple<Player, Party, Sdk>) -> C1 = { triple ->
    also {
        player = triple.first
        party = triple.second
        sdk = triple.third
    }
}

abstract class PlayerContext : PartyContext() {
    lateinit var player: Player
}
