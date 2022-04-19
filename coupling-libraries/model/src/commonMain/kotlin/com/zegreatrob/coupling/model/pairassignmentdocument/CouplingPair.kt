package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

fun pairOf(player1: Player) = CouplingPair.Single(player1)

fun pairOf(player1: Player, player2: Player) = CouplingPair.Double(player1, player2)

sealed class CouplingPair {

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

data class PinnedCouplingPair(val players: List<PinnedPlayer>, val pins: List<Pin> = emptyList()) {

    fun toPair() = when (this.players.size) {
        2 -> pairOf(players[0].player, players[1].player)
        1 -> pairOf(players[0].player)
        else -> CouplingPair.Empty
    }
}

fun List<CouplingPair>.withPins() = map { it.withPins() }

fun CouplingPair.withPins(pins: List<Pin> = emptyList()) = PinnedCouplingPair(
    asArray().map { player -> player.withPins() },
    pins
)
