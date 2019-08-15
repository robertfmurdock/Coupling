package com.zegreatrob.coupling.client.stats

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.common.ComposeStatisticsAction
import com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.common.PairReport
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findComponent
import shallow
import kotlin.test.Test

class TribeStatisticsTest : CalculateHeatMapActionDispatcher, ComposeStatisticsActionDispatcher {

    @Test
    fun willShowTribeCard() = setup(object : TribeStatisticsBuilder,
        PropsClassProvider<TribeStatisticsProps> by provider() {
        val tribe = KtTribe(TribeId("1"))
        val props = TribeStatisticsProps(
            StatisticQueryResults(
                tribe = tribe,
                players = emptyList(),
                history = emptyList(),
                heatmapData = CalculateHeatMapAction(emptyList(), emptyList(), 0).perform(),
                report = ComposeStatisticsAction(tribe, emptyList(), emptyList()).perform()
            )
        ) {}
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(TribeCard)
            .props()
            .tribe.assertIsEqualTo(tribe)
    }

    @Test
    fun willShowPairings() = setup(object : TribeStatisticsBuilder, ComposeStatisticsActionDispatcher,
        PropsClassProvider<TribeStatisticsProps> by provider() {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val tribe = KtTribe(TribeId("1"), name = "Mathematica")
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
        shallow(props)
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
    fun sendsPlayerHeatDataToSubComponent() = setup(object : TribeStatisticsBuilder,
        PropsClassProvider<TribeStatisticsProps> by provider() {
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
        val tribe = KtTribe(TribeId("2"), name = "Mathematica")

        val report = ComposeStatisticsAction(tribe, players, history).perform()
        val props = TribeStatisticsProps(StatisticQueryResults(
            tribe = tribe,
            players = players,
            history = history,
            heatmapData = CalculateHeatMapAction(players, history, report.spinsUntilFullRotation).perform(),
            report = report
        ),
            pathSetter = {}
        )
    }) exercise {
        shallow(props)
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
    fun willShowBasicStatisticsOnSubComponent() = setup(object : TribeStatisticsBuilder,
        ComposeStatisticsActionDispatcher,
        PropsClassProvider<TribeStatisticsProps> by provider() {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val tribe = KtTribe(TribeId("2"), name = "Mathematica")
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
        shallow(props)
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
    fun willShowTheMedianSpinTimeOnSubComponent() = setup(object : TribeStatisticsBuilder,
        PropsClassProvider<TribeStatisticsProps> by provider() {
        val players = listOf(
            Player("harry", name = "Harry"),
            Player("larry", name = "Larry"),
            Player("curry", name = "Curly"),
            Player("moe", name = "Moe")
        )
        val tribe = KtTribe(TribeId("2"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                pairs = listOf(pairOf(players[0], players[1]), pairOf(players[2], players[3])).withNoPins(),
                date = DateTime(2017, 3, 14)
            ),
            PairAssignmentDocument(
                pairs = listOf(pairOf(players[0], players[1]), pairOf(players[2], players[3])).withNoPins(),
                date = DateTime(2017, 3, 12)
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
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(TeamStatistics)
            .props()
            .medianSpinDuration
            .assertIsEqualTo("2 days")
    }
}


private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }

