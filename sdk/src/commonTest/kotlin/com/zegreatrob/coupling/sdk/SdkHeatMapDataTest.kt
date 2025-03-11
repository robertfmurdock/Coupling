package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.stats.heatmap.heatmapData
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentId
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class SdkHeatMapDataTest {

    companion object : AssignPinsAction.Dispatcher {
        private fun pairAssignmentDocument(player1: Player, player2: Player) = PairAssignmentDocument(
            id = stubPairAssignmentId(),
            date = Clock.System.now(),
            pairs = notEmptyListOf(pairOf(player1, player2)).withPins(),
            null,
        )
    }

    @Test
    fun withNoPlayersReturnsNoData() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = emptyList<Player>()
        val history = emptyList<PairAssignmentDocument>()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(graphQuery { party(party.id) { pairs { recentTimesPaired() } } })
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(emptyList<List<Int?>>())
    }

    @Test
    fun withOnePlayerProducesOneRowWith10() = asyncSetup(object {
        val party = stubPartyDetails()
        val players = listOf(stubPlayer())
        val history = emptyList<PairAssignmentDocument>()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        recentTimesPaired()
                    }
                }
            },
        )
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(
                listOf(
                    listOf(null),
                ),
            )
    }

    @Test
    fun withThreePlayersAndNoHistoryProducesThreeRows() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
            stubPlayer(),
        )
        val history = emptyList<PairAssignmentDocument>()
        val party = stubPartyDetails()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        recentTimesPaired()
                    }
                }
            },
        )
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(
                listOf(
                    listOf(null, 0.0, 0.0),
                    listOf(0.0, null, 0.0),
                    listOf(0.0, 0.0, null),
                ),
            )
    }

    @Test
    fun withTwoPlayersAndShortHistoryProducesTwoRowsWithHeatValues() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
        )
        val history = listOf(pairAssignmentDocument(players[0], players[1]))
        val party = stubPartyDetails()
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        recentTimesPaired()
                    }
                }
            },
        )
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(
                listOf(
                    listOf(null, 1.0),
                    listOf(1.0, null),
                ),
            )
    }

    @Test
    fun withTwoPlayersAndFullHistoryProducesTwoRowsWithHeatValues() = asyncSetup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
        )
        val party = stubPartyDetails()
        val history = listOf(
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
        )
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    pairs {
                        players()
                        recentTimesPaired()
                    }
                }
            },
        )
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(
                listOf(
                    listOf(null, 10.0),
                    listOf(10.0, null),
                ),
            )
    }

    @Test
    fun withThreePlayersAndInterestingHistoryProducesThreeRowsWithHeatValues() = asyncSetup(object {
        val players = listOf(
            stubPlayer().copy(name = "0"),
            stubPlayer().copy(name = "1"),
            stubPlayer().copy(name = "2"),
        )
        val party = stubPartyDetails()
        val history = listOf(
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[2]),
        )
    }) {
        savePartyState(party, players, history)
    } exercise {
        sdk().fire(
            graphQuery {
                party(party.id) {
                    spinsUntilFullRotation()
                    pairs {
                        players()
                        recentTimesPaired()
                    }
                }
            },
        )
    } verify { result ->
        heatmapData(players, result?.party?.pairs!!)
            .assertIsEqualTo(
                listOf(
                    listOf(null, 10.0, 1.0),
                    listOf(10.0, null, 0.0),
                    listOf(1.0, 0.0, null),
                ),
            )
    }
}
