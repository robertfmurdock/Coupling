package com.zegreatrob.coupling.client

import ShallowWrapper
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.stats.*
import com.zegreatrob.coupling.client.tribe.TribeCardRenderer
import com.zegreatrob.coupling.common.PairReport
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import react.RProps
import shallow
import kotlin.js.Json
import kotlin.test.Test

class TribeStatisticsBuilderTest {

    @Test
    fun willShowTribeCard() = setup(object : TribeStatisticsBuilder {
        val tribe = KtTribe(TribeId("1"))
        val props = TribeStatisticsProps(tribe, emptyList(), emptyList()) {}
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(TribeCardRenderer.tribeCard)
                .props()
                .tribe.assertIsEqualTo(tribe)
    }

    @Test
    fun willShowPairings() = setup(object : TribeStatisticsBuilder {
        val players = listOf(
                Player("harry", name = "Harry"),
                Player("larry", name = "Larry"),
                Player("curry", name = "Curly"),
                Player("moe", name = "Moe")
        )
        val props = TribeStatisticsProps(
                tribe = KtTribe(TribeId("1"), name = "Mathematica"),
                players = players,
                history = listOf(
                        PairAssignmentDocument(
                                date = DateTime.now(),
                                pairs = listOf<CouplingPair>(
                                        CouplingPair.Double(players[0], players[1]),
                                        CouplingPair.Double(players[2], players[3])
                                ).withNoPins()
                        )
                )
        ) {}
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(PairReportTableSyntax.component)
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
    fun sendsPlayerHeatDataToSubComponent() = setup(object : TribeStatisticsBuilder {
        val players = listOf(
                Player("harry", name = "Harry"),
                Player("larry", name = "Larry"),
                Player("curry", name = "Curly"),
                Player("moe", name = "Moe")
        )
        val props = TribeStatisticsProps(
                tribe = KtTribe(TribeId("2"), name = "Mathematica"),
                players = players,
                history = listOf(
                        PairAssignmentDocument(
                                date = DateTime.now(),
                                pairs = listOf(
                                        pairOf(players[0], players[1]),
                                        pairOf(players[2], players[3])
                                ).withNoPins()
                        )
                ),
                pathSetter = {}
        )
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.find(PlayerHeatmapSyntax.rClass)
                .props()
                .unsafeCast<Json>()["heatmapData"]
                .unsafeCast<Array<Array<Int?>>>()
                .map { it.toList() }
                .toList()
                .assertIsEqualTo(listOf(
                        listOf(null, 1, 0, 0),
                        listOf(1, null, 0, 0),
                        listOf(0, 0, null, 1),
                        listOf(0, 0, 1, null)
                ))
    }

    @Test
    fun willShowBasicStatisticsOnSubComponent() = setup(object : TribeStatisticsBuilder {
        val players = listOf(
                Player("harry", name = "Harry"),
                Player("larry", name = "Larry"),
                Player("curry", name = "Curly"),
                Player("moe", name = "Moe")
        )
        val props = TribeStatisticsProps(
                tribe = KtTribe(TribeId("2"), name = "Mathematica"),
                players = players,
                history = emptyList(),
                pathSetter = {}
        )
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(TeamStatisticsSyntax.component)
                .props()
                .apply {
                    activePlayerCount
                            .assertIsEqualTo(4)
                    spinsUntilFullRotation
                            .assertIsEqualTo(3)
                }
    }

    @Test
    fun willShowTheMedianSpinTimeOnSubComponent() = setup(object : TribeStatisticsBuilder {
        val players = listOf(
                Player("harry", name = "Harry"),
                Player("larry", name = "Larry"),
                Player("curry", name = "Curly"),
                Player("moe", name = "Moe")
        )
        val props = TribeStatisticsProps(
                tribe = KtTribe(TribeId("2"), name = "Mathematica"),
                players = players,
                history = listOf(
                        PairAssignmentDocument(
                                pairs = listOf(pairOf(players[0], players[1]), pairOf(players[2], players[3])).withNoPins(),
                                date = DateTime(2017, 3, 14)
                        ),
                        PairAssignmentDocument(
                                pairs = listOf(pairOf(players[0], players[1]), pairOf(players[2], players[3])).withNoPins(),
                                date = DateTime(2017, 3, 12)
                        )
                ),
                pathSetter = {}
        )
    }) exercise {
        shallow(props)
    } verify { wrapper ->
        wrapper.findComponent(TeamStatisticsSyntax.component)
                .props()
                .medianSpinDuration
                .assertIsEqualTo("2 days")
    }
}


private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }

fun <P : RProps> ShallowWrapper<dynamic>.findComponent(
        reactFunctionComponent: ReactFunctionComponent<P>
): ShallowWrapper<P> = find(reactFunctionComponent.rFunction)
