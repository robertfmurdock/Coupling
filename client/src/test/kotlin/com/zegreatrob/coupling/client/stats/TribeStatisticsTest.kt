package com.zegreatrob.coupling.client.stats

import com.benasher44.uuid.Uuid
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.ComposeStatisticsAction
import com.zegreatrob.coupling.action.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.action.PairReport
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findComponent
import shallow
import kotlin.test.Test

class TribeStatisticsTest : CalculateHeatMapActionDispatcher, ComposeStatisticsActionDispatcher {

    override val traceId: Uuid? = null

    @Test
    fun willShowTribeCard() = setup(object {
        val tribe = Tribe(TribeId("1"))
        val props = TribeStatisticsProps(
            StatisticQueryResults(
                tribe = tribe,
                players = emptyList(),
                history = emptyList(),
                heatmapData = CalculateHeatMapAction(
                    emptyList(),
                    emptyList(),
                    0
                ).perform(),
                report = ComposeStatisticsAction(tribe, emptyList(), emptyList()).perform()
            )
        ) {}
    }) exercise {
        shallow(TribeStatistics, props)
    } verify { wrapper ->
        wrapper.findComponent(TribeCard)
            .props()
            .tribe.assertIsEqualTo(tribe)
    }

    @Test
    fun willShowPairings() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val tribe = Tribe(
            TribeId("1"),
            name = "Mathematica"
        )
        val history = listOf(
            PairAssignmentDocument(
                date = DateTime.now(),
                pairs = listOf<CouplingPair>(
                    CouplingPair.Double(players[0], players[1]),
                    CouplingPair.Double(players[2], players[3])
                ).withNoPins()
            )
        )
        val props = TribeStatisticsProps(
            queryResults = StatisticQueryResults(
                tribe = tribe,
                players = players,
                history = history,
                heatmapData = emptyList(),
                report = ComposeStatisticsAction(tribe, players, history).perform()
            )

        ) {}
    }) exercise {
        shallow(TribeStatistics, props)
    } verify { wrapper ->
        wrapper.findComponent(PairReportTable)
            .props()
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
                date = DateTime.now(),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            )
        )
        val tribe = Tribe(
            TribeId("2"),
            name = "Mathematica"
        )

        val report = ComposeStatisticsAction(tribe, players, history).perform()
        val props = TribeStatisticsProps(StatisticQueryResults(
            tribe = tribe,
            players = players,
            history = history,
            heatmapData = CalculateHeatMapAction(
                players,
                history,
                report.spinsUntilFullRotation
            ).perform(),
            report = report
        ),
            pathSetter = {}
        )
    }) exercise {
        shallow(TribeStatistics, props)
    } verify { wrapper ->
        wrapper.findComponent(PlayerHeatmap)
            .props()
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
        val tribe = Tribe(
            TribeId("2"),
            name = "Mathematica"
        )
        val props = TribeStatisticsProps(
            StatisticQueryResults(
                tribe = tribe,
                players = players,
                history = emptyList(),
                heatmapData = emptyList(),
                report = ComposeStatisticsAction(tribe, players, emptyList()).perform()
            ),
            pathSetter = {}
        )
    }) exercise {
        shallow(TribeStatistics, props)
    } verify { wrapper ->
        wrapper.findComponent(TeamStatistics)
            .props()
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
        val tribe = Tribe(
            TribeId("2"),
            name = "Mathematica"
        )
        val history = listOf(
            PairAssignmentDocument(
                date = DateTime(2017, 3, 14),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            ),
            PairAssignmentDocument(
                date = DateTime(2017, 3, 12),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3])
                ).withNoPins()
            )
        )
        val props = TribeStatisticsProps(
            StatisticQueryResults(
                tribe = tribe,
                players = players,
                history = history,
                heatmapData = emptyList(),
                report = ComposeStatisticsAction(tribe, players, history).perform()
            ),
            pathSetter = {}
        )
    }) exercise {
        shallow(TribeStatistics, props)
    } verify { wrapper ->
        wrapper.findComponent(TeamStatistics)
            .props()
            .medianSpinDuration
            .assertIsEqualTo("2 days")
    }
}


private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() =
    PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }

