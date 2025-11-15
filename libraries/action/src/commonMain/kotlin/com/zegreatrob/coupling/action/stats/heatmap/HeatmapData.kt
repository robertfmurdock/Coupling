package com.zegreatrob.coupling.action.stats.heatmap

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.player.Player
import kotlin.math.min

fun heatmapData(players: List<Player>, pairs: List<PlayerPair>) = players.map { player1 ->
    players.map { player2 -> findPair(player1, player2, pairs) }
        .map { it?.recentTimesPaired }
        .map { it?.toHeatIncrement() }
}

private fun findPair(
    player1: Player,
    player2: Player,
    playerPairs: List<PlayerPair>?,
) = playerPairs?.firstOrNull { pair ->
    pair.players.elements.map { it.id }.toSet() == setOf(player1.id, player2.id)
}

val heatIncrements = listOf(0.0, 1.0, 2.5, 4.5, 7.0, 10.0)
private fun Int.toHeatIncrement() = heatIncrements[incrementIndex(this)]

private fun incrementIndex(timesPaired: Int) = min(timesPaired, heatIncrements.size - 1)
