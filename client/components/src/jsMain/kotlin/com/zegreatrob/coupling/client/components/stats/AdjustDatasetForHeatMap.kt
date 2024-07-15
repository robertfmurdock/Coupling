package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.external.nivo.NivoPoint
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.player.Player
import kotlinx.datetime.Clock
import kotlin.time.Duration

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

private const val WORKDAYS_PER_WEEK = 5
private const val EXCELLENT_CONTRIBUTIONS_PER_DAY = 4

fun Map<Set<Player>, List<Contribution>>.toNivoHeatmapSettings(window: JsonContributionWindow): Pair<Int, Array<NivoHeatMapData>> {
    val players = keys.flatten().toSet()

    val max = WORKDAYS_PER_WEEK * EXCELLENT_CONTRIBUTIONS_PER_DAY * window.weeks(this)

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

private fun JsonContributionWindow.weeks(map: Map<Set<Player>, List<Contribution>>) = toModel()
    ?.inWholeWeeks()
    ?: map.weeksSinceFirstContribution()

private fun Map<Set<Player>, List<Contribution>>.weeksSinceFirstContribution(): Int =
    (Clock.System.now() - firstContributionInstant()).inWholeWeeks()

private fun Map<Set<Player>, List<Contribution>>.firstContributionInstant() =
    values.flatten()
        .mapNotNull { it.dateTime }
        .minOrNull()
        ?: Clock.System.now()

private fun Duration.inWholeWeeks(): Int = (inWholeDays.toInt() / 7)
