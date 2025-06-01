package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoPoint
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import kotlinx.datetime.Clock
import kotlin.time.Duration

fun adjustDatasetForHeatMap(
    contributionMap: Map<CouplingPair, List<Contribution>>,
): Map<Set<Player>, List<Contribution>> {
    val contributionSet = contributionMap.mapKeys { it.key.toSet() }
    return mergeMobContributionsToRelevantPairs(
        nonMobContributionSet = contributionSet.filterKeys { it.size <= 2 },
        mobContributionSet = contributionSet.filterKeys { it.size > 2 },
        empties = contributionMap.missingPairings().map { it to emptyList() },
    ).filterValues(List<Contribution>::isNotEmpty)
}

private fun mergeMobContributionsToRelevantPairs(
    nonMobContributionSet: Map<Set<Player>, List<Contribution>>,
    empties: List<Pair<Set<Player>, List<Contribution>>>,
    mobContributionSet: Map<Set<Player>, List<Contribution>>,
) = (nonMobContributionSet + empties).mapValues(mobContributionSet.addToPairsTransform()).toMap() + mobContributionSet

private fun Map<CouplingPair, List<Contribution>>.missingPairings(): Set<Set<Player>> {
    val players = keys.flatMap(CouplingPair::toSet)
    val allPairs = players.flatMap { player1 -> players.map { player2 -> setOf(player1, player2) } }.toSet()
    return (allPairs - keys.map(CouplingPair::toSet).toSet()).toSet()
}

private fun Map<Set<Player>, List<Contribution>>.addToPairsTransform() = { (players, contributions): Map.Entry<Set<Player>, List<Contribution>> ->
    contributions + flatMap { (mob, mobContributions) ->
        if (players.size == 2 && mob.containsAll(players)) {
            mobContributions
        } else {
            emptySet()
        }
    }
}

private const val WORKDAYS_PER_WEEK = 5
private const val EXCELLENT_CONTRIBUTIONS_PER_DAY = 4

fun Map<Set<Player>, List<Contribution>>.toNivoHeatmapSettings(
    window: GqlContributionWindow,
    spinsUntilFullRotation: Int,
    yConverter: (List<Contribution>) -> Number,
): Pair<Int, Array<NivoHeatMapData>> {
    val players = keys.flatten().toSet()

    val max = (WORKDAYS_PER_WEEK * EXCELLENT_CONTRIBUTIONS_PER_DAY * window.weeks(this)) / (spinsUntilFullRotation)

    val data: Array<NivoHeatMapData> = players.map { player1 ->
        NivoHeatMapData(
            id = pairOf(player1).pairId,
            data = players.map { player2 ->
                val contributions = this[setOf(player1, player2)]
                NivoPoint(
                    x = pairOf(player2).pairId,
                    y = contributions?.let(yConverter),
                )
            }.toTypedArray(),
        )
    }.toTypedArray()
    return Pair(max, data)
}

private fun GqlContributionWindow.weeks(map: Map<Set<Player>, List<Contribution>>) = toModel()
    ?.inWholeWeeks()
    ?: map.weeksSinceFirstContribution()

private fun Map<Set<Player>, List<Contribution>>.weeksSinceFirstContribution(): Int = (Clock.System.now() - values.flatten().firstContributionInstant()).inWholeWeeks()

fun List<Contribution>.firstContributionInstant() = mapNotNull { it.dateTime }
    .minOrNull()
    ?: Clock.System.now()

private fun Duration.inWholeWeeks(): Int = (inWholeDays.toInt() / 7)
