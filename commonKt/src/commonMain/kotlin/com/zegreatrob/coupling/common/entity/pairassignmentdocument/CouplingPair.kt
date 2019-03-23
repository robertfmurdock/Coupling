package com.zegreatrob.coupling.common.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.player.Player
import kotlin.js.JsName

sealed class CouplingPair {
    @JsName("asArray")
    abstract fun asArray(): Array<Player>

    object Empty : CouplingPair() {
        override fun asArray() = arrayOf<Player>()
    }

    data class Single(val player: Player) : CouplingPair() {
        override fun asArray() = arrayOf(player)
    }

    data class Double(val player1: Player, val player2: Player) : CouplingPair() {
        override fun asArray() = arrayOf(player1, player2)
    }

    companion object : CouplingComparisionSyntax {
        fun equivalent(pair1: CouplingPair, pair2: CouplingPair) = areEqualPairs(pair1, pair2)
    }
}

fun Player.withPins(pins: List<Pin> = emptyList()) = PinnedPlayer(this, pins)

data class PinnedPlayer(val player: Player, val pins: List<Pin>)

data class PinnedCouplingPair(val players: List<PinnedPlayer>) {

    fun toPair() = when (this.players.size) {
        2 -> CouplingPair.Double(players[0].player, players[1].player)
        1 -> CouplingPair.Single(players[0].player)
        else -> CouplingPair.Empty
    }
}