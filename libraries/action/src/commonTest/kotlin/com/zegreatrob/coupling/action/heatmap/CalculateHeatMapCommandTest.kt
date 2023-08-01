package com.zegreatrob.coupling.action.heatmap

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.datetime.Clock
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class CalculateHeatMapCommandTest {

    companion object : CalculateHeatMapAction.Dispatcher, AssignPinsAction.Dispatcher {
        private suspend fun pairAssignmentDocument(player1: Player, player2: Player) =
            PairAssignmentDocument(
                id = PairAssignmentDocumentId(""),
                date = Clock.System.now(),
                pairs = perform(
                    AssignPinsAction(notEmptyListOf(pairOf(player1, player2)), emptyList(), emptyList()),
                ),
                null,
            )
    }

    @Test
    fun withNoPlayersReturnsNoData() = setup(object {
        val players = emptyList<Player>()
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 0
        val action = CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(emptyList<List<Int?>>())
    }

    @Test
    fun withOnePlayerProducesOneRowWithNull() = setup(object {
        val players = listOf(Player(id = "0", avatarType = null))
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 0
        val action =
            CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                listOf(null),
            ),
        )
    }

    @Test
    fun withThreePlayersAndNoHistoryProducesThreeRows() = setup(object {
        val players = listOf(
            Player(id = "0", avatarType = null),
            Player(id = "1", avatarType = null),
            Player(id = "2", avatarType = null),
        )
        val history = emptyList<PairAssignmentDocument>()
        val rotationPeriod = 3
        val action =
            CalculateHeatMapAction(players, history, rotationPeriod)
    }) exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
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
            Player(id = "0", avatarType = null),
            Player(id = "1", avatarType = null),
        )
        val rotationPeriod = 1
        lateinit var action: CalculateHeatMapAction
    }) {
        val history = listOf(pairAssignmentDocument(players[0], players[1]))
        action = CalculateHeatMapAction(players, history, rotationPeriod)
    } exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                listOf(null, 1.0),
                listOf(1.0, null),
            ),
        )
    }

    @Test
    fun withTwoPlayersAndFullHistoryProducesTwoRowsWithHeatValues() = asyncSetup(object {
        val players = listOf(
            Player(id = "0", avatarType = null),
            Player(id = "1", avatarType = null),
        )
        val rotationPeriod = 1
        lateinit var action: CalculateHeatMapAction
    }) {
        val history = listOf(
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
            pairAssignmentDocument(players[0], players[1]),
        )
        action = CalculateHeatMapAction(players, history, rotationPeriod)
    } exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                listOf(null, 10.0),
                listOf(10.0, null),
            ),
        )
    }

    @Test
    fun withThreePlayersAndInterestingHistoryProducesThreeRowsWithHeatValues() = asyncSetup(object {
        val players = listOf(
            Player(id = "0", avatarType = null),
            Player(id = "1", avatarType = null),
            Player(id = "2", avatarType = null),
        )
        val rotationPeriod = 3
        lateinit var action: CalculateHeatMapAction
    }) {
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
        action = CalculateHeatMapAction(players, history, rotationPeriod)
    } exercise {
        perform(action)
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                listOf(null, 10.0, 1.0),
                listOf(10.0, null, 0.0),
                listOf(1.0, 0.0, null),
            ),
        )
    }
}
