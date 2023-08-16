package com.zegreatrob.coupling.action.stats.heatmap

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.player.Player

fun heatmapData(players: List<Player>, pairs: List<PlayerPair>) = players.map { player1 ->
    players.map { player2 -> findPair(player1, player2, pairs) }
        .map { it?.heat }
}

private fun findPair(
    player1: Player,
    player2: Player,
    playerPairs: List<PlayerPair>?,
) = playerPairs?.first { pair ->
    pair.players?.elements?.map { it.id }?.toSet() == setOf(player1.id, player2.id)
}
