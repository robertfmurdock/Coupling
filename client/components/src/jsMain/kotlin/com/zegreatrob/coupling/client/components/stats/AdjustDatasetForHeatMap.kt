package com.zegreatrob.coupling.client.components.stats

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
