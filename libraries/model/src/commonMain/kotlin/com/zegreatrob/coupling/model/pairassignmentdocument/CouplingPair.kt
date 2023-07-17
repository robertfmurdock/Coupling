package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import kotools.types.collection.NotEmptyList

fun pairOf(player1: Player) = CouplingPair.Single(player1)

fun pairOf(player1: Player, player2: Player) = CouplingPair.Double(player1, player2)

sealed class CouplingPair {

    abstract fun asArray(): Array<Player>

    data object Empty : CouplingPair() {
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

data class PinnedCouplingPair(val pinnedPlayers: List<PinnedPlayer>, val pins: Set<Pin> = emptySet()) {

    fun toPair() = when (this.pinnedPlayers.size) {
        2 -> pairOf(pinnedPlayers[0].player, pinnedPlayers[1].player)
        1 -> pairOf(pinnedPlayers[0].player)
        else -> CouplingPair.Empty
    }
}

val PinnedCouplingPair.players get() = pinnedPlayers.map(PinnedPlayer::player)

fun NotEmptyList<CouplingPair>.withPins() = map(CouplingPair::withPins)

fun CouplingPair.withPins(pins: Set<Pin> = emptySet()) = PinnedCouplingPair(
    asArray().map { player -> player.withPins() },
    pins,
)

fun PinnedCouplingPair.callSign(): CallSign? {
    val nounPlayer = toPair().asArray().getOrNull(0)
    val adjectivePlayer = toPair().asArray().getOrNull(1) ?: nounPlayer

    val adjective = adjectivePlayer?.callSignAdjective
    val noun = nounPlayer?.callSignNoun
    return if (adjective != null && noun != null) {
        CallSign(adjective, noun)
    } else {
        null
    }
}
