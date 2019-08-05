package com.zegreatrob.coupling.common.entity.heatmap

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinAssignmentSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CalculateHeatMapCommandTest {

    companion object : CalculateHeatMapActionDispatcher, PinAssignmentSyntax {
        private fun pairAssignmentDocument(player1: Player, player2: Player) =
                PairAssignmentDocument(
                        date = DateTime.now(),
                        pairs = listOf(CouplingPair.Double(player1, player2)).assign(emptyList())
                )
    }

    @Test
    fun withNoPlayersReturnsNoData() = setup(object {
        val players = emptyList<Player>()
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 0
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(emptyList<List<Int?>>())
    }

    @Test
    fun withOnePlayerProducesOneRowWithNull() = setup(object {
        val players = listOf(Player(id = "0"))
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 0
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(
                        listOf(null)
                )
        )
    }

    @Test
    fun withThreePlayersAndNoHistoryProducesThreeRows() = setup(object {
        val players = listOf(
                Player(id = "0"),
                Player(id = "1"),
                Player(id = "2")
        )
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 3
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(
                        listOf(null, 0.0, 0.0),
                        listOf(0.0, null, 0.0),
                        listOf(0.0, 0.0, null)
                )
        )
    }

    @Test
    fun withTwoPlayersAndShortHistoryProducesTwoRowsWithHeatValues() = setup(object {
        val players = listOf(
                Player(id = "0"),
                Player(id = "1")
        )
        val history = listOf(pairAssignmentDocument(players[0], players[1]))
        val rotationPeriod = 1
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(
                        listOf(null, 1.0),
                        listOf(1.0, null)
                )
        )
    }

    @Test
    fun withTwoPlayersAndFullHistoryProducesTwoRowsWithHeatValues() = setup(object {
        val players = listOf(
                Player(id = "0"),
                Player(id = "1")
        )
        val history = listOf(
                pairAssignmentDocument(players[0], players[1]),
                pairAssignmentDocument(players[0], players[1]),
                pairAssignmentDocument(players[0], players[1]),
                pairAssignmentDocument(players[0], players[1]),
                pairAssignmentDocument(players[0], players[1])
        )
        val rotationPeriod = 1
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(
                        listOf(null, 10.0),
                        listOf(10.0, null)
                )
        )
    }

    @Test
    fun withThreePlayersAndInterestingHistoryProducesThreeRowsWithHeatValues() = setup(object {
        val players = listOf(
                Player(id = "0"),
                Player(id = "1"),
                Player(id = "2")
        )
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
                pairAssignmentDocument(players[0], players[2])
        )
        val rotationPeriod = 3
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        action.perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(
                        listOf(null, 10.0, 1.0),
                        listOf(10.0, null, 0.0),
                        listOf(1.0, 0.0, null)
                )
        )
    }

}