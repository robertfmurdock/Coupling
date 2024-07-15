package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.external.nivo.NivoPoint
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.player.Player

fun adjustDatasetForHeatMap(
    contributionMap: Map<CouplingPair, List<Contribution>>,
): Map<Set<Player>, List<Contribution>> {
    val contributionSet: Map<Set<Player>, List<Contribution>> = contributionMap.mapKeys { it.key.asArray().toSet() }

    val nonMobContributionSet = contributionSet.filterKeys { it.size <= 2 }
    val mobContributionSet = contributionSet.filterKeys { it.size > 2 }

    val players = contributionMap.keys.flatMap(CouplingPair::toSet)

    val allPairs = players.flatMap { player1 -> players.map { player2 -> setOf(player1, player2) } }.toSet()

    val missingPairs: Set<Set<Player>> = (allPairs - contributionMap.keys.map(CouplingPair::toSet).toSet()).toSet()
    val empties = missingPairs.map { it to emptyList<Contribution>() }

    return (nonMobContributionSet + empties).map { (players, contributions) ->
        players to contributions + mobContributionSet.flatMap { (mob, mobContributions) ->
            if (players.size == 2 && mob.containsAll(players)) {
                mobContributions
            } else {
                emptySet()
            }
        }
    }.toMap() + mobContributionSet
}

fun Map<Set<Player>, List<Contribution>>.toNivoHeatmapSettings(): Pair<Int, Array<NivoHeatMapData>> {
    val players = keys.flatten().toSet()

    val max = values.maxOfOrNull { it.size } ?: 10

    val data: Array<NivoHeatMapData> = players.map { player1 ->
        NivoHeatMapData(
            id = player1.name,
            data = players.map { player2 ->
                NivoPoint(
                    x = player2.name,
                    y = this[setOf(player1, player2)]?.size?.let { max - it },
                )
            }.toTypedArray(),
        )
    }.toTypedArray()
    return Pair(max, data)
}
