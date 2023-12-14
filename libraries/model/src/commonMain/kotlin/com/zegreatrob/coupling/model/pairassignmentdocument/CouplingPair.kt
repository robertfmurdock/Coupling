package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf

fun pairOf(player1: Player) = CouplingPair.Single(player1)

fun pairOf(player1: Player, player2: Player) = CouplingPair.Double(player1, player2)

sealed class CouplingPair : Iterable<Player> {

    abstract fun toNotEmptyList(): NotEmptyList<Player>
    fun asArray(): Array<Player> = toNotEmptyList().toList().toTypedArray()
    override fun iterator(): Iterator<Player> = toNotEmptyList().toList().iterator()

    data class Single(val player: Player) : CouplingPair() {
        override fun toNotEmptyList() = notEmptyListOf(player)
    }

    data class Double(val player1: Player, val player2: Player) : CouplingPair() {
        override fun toNotEmptyList() = notEmptyListOf(player1, player2)
    }

    data class Mob(val player1: Player, val player2: Player, val player3: Player, val more: Set<Player>) :
        CouplingPair() {
        override fun toNotEmptyList() =
            notEmptyListOf(player1, tail = listOf(player2, player3).plus(more).toTypedArray())
    }

    companion object {
        fun equivalent(pair1: CouplingPair, pair2: CouplingPair) = areEqualPairs(pair1, pair2)
    }
}

fun List<Player>.toCouplingPair() = when (size) {
    1 -> CouplingPair.Single(first())
    2 -> CouplingPair.Double(this[0], this[1])
    else -> CouplingPair.Mob(this[0], this[1], this[2], slice(3..<size).toSet())
}

fun Player.withPins(pins: List<Pin> = emptyList()) = PinnedPlayer(this, pins)

data class PinnedPlayer(val player: Player, val pins: List<Pin>)

data class PinnedCouplingPair(val pinnedPlayers: NotEmptyList<PinnedPlayer>, val pins: Set<Pin> = emptySet()) {

    fun toPair(): CouplingPair = with(pinnedPlayers) {
        when (val tail = tail) {
            null -> pairOf(head.player)
            else -> pairOf(head.player, tail.head.player)
        }
    }
}

val PinnedCouplingPair.players get() = pinnedPlayers.map(PinnedPlayer::player)

fun NotEmptyList<CouplingPair>.withPins() = map(CouplingPair::withPins)

fun CouplingPair.withPins(pins: Set<Pin> = emptySet()) = PinnedCouplingPair(
    toNotEmptyList().map { player -> player.withPins() },
    pins,
)

fun PinnedCouplingPair.callSign(): CallSign {
    val pair = toPair().toNotEmptyList()
    val nounPlayer = pair.head
    val adjectivePlayer = pair.tail?.head ?: nounPlayer

    val adjective = adjectivePlayer.callSignAdjective
    val noun = nounPlayer.callSignNoun
    return CallSign(adjective, noun)
}

val CouplingPair.pairId get() = joinToString("-", transform = Player::id)
