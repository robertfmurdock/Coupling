package com.zegreatrob.coupling.client.stats

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.ComposeStatisticsAction
import com.zegreatrob.coupling.action.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PartyStatisticsTest : CalculateHeatMapActionDispatcher, ComposeStatisticsActionDispatcher {

    @Test
    fun willShowPairings() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val party = Party(PartyId("1"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime.now(),
                pairs = listOf<CouplingPair>(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            )
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        shallow(PartyStatistics(StatisticQueryResults(party, players, history, report, emptyList())))
    } verify { wrapper ->
        wrapper.find(pairReportTable)
            .dataprops()
            .pairReports
            .assertIsOrderedByLongestTimeSinceLastPairing()
            .assertHasTheTimeSincePairLastOccurred()
    }

    private fun List<PairReport>.assertIsOrderedByLongestTimeSinceLastPairing() = also {
        map { report -> report.pair.asArray().map { player -> player.name } }
            .assertIsEqualTo(
                listOf(
                    listOf("Harry", "Curly"),
                    listOf("Harry", "Moe"),
                    listOf("Larry", "Curly"),
                    listOf("Larry", "Moe"),
                    listOf("Harry", "Larry"),
                    listOf("Curly", "Moe")
                )
            )
    }

    private fun List<PairReport>.assertHasTheTimeSincePairLastOccurred() = also {
        map { it.timeSinceLastPair }
            .assertIsEqualTo(
                listOf(
                    NeverPaired,
                    NeverPaired,
                    NeverPaired,
                    NeverPaired,
                    TimeResultValue(0),
                    TimeResultValue(0)
                )
            )
    }

    @Test
    fun sendsPlayerHeatDataToSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime.now(),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            )
        )
        val party = Party(PartyId("2"), name = "Mathematica")
        val report = perform(ComposeStatisticsAction(party, players, history))
        val heatmapData = perform(CalculateHeatMapAction(players, history, report.spinsUntilFullRotation))
    }) exercise {
        shallow(PartyStatistics(StatisticQueryResults(party, players, history, report, heatmapData)))
    } verify { wrapper ->
        wrapper.find(playerHeatmap)
            .dataprops()
            .heatmapData
            .assertIsEqualTo(
                listOf(
                    listOf(null, 1, 0, 0),
                    listOf(1, null, 0, 0),
                    listOf(0, 0, null, 1),
                    listOf(0, 0, 1, null)
                )
            )
    }

    @Test
    fun willShowBasicStatisticsOnSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val party = Party(
            PartyId("2"),
            name = "Mathematica"
        )
        val report = perform(ComposeStatisticsAction(party, players, emptyList()))
    }) exercise {
        shallow(PartyStatistics(StatisticQueryResults(party, players, emptyList(), report, emptyList())))
    } verify { wrapper ->
        wrapper.find(teamStatistics)
            .dataprops()
            .apply {
                activePlayerCount
                    .assertIsEqualTo(4)
                spinsUntilFullRotation
                    .assertIsEqualTo(3)
            }
    }

    @Test
    fun willShowTheMedianSpinTimeOnSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val party = Party(PartyId("2"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2017, 3, 14),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2017, 3, 12),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            )
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        shallow(PartyStatistics(StatisticQueryResults(party, players, history, report, emptyList())))
    } verify { wrapper ->
        wrapper.find(teamStatistics)
            .dataprops()
            .medianSpinDuration
            .assertIsEqualTo("2 days")
    }
}

private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }
