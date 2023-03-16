package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.test.assertEquals

class FindNewPairsActionTest {

    @Test
    fun withNoPlayersShouldReturnNoPairs() = setup(object : FindNewPairsActionDispatcher, Wheel {
        override val execute = stubActionExecutor(NextPlayerAction::class)
        override val wheel = this
    }) exercise {
        perform(FindNewPairsAction(Game(listOf(), listOf(), PairingRule.LongestTime)))
    } verify { assertEquals(it, listOf()) }

    @Test
    fun withTwoPlayersEachShouldBeRemovedFromWheelBeforeEachPlay() = setup(object : FindNewPairsActionDispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)
        override val wheel = StubWheel()
        val bill: Player = Player(id = "Bill")
        val ted: Player = Player(id = "Ted")
        val players = listOf(bill, ted)
    }) {
        wheel.spyReturnValues.add(bill)
        execute.spyReturnValues.add(PairCandidateReport(ted, listOf(bill), TimeResultValue(0)))
    } exercise {
        perform(FindNewPairsAction(Game(listOf(), players, PairingRule.LongestTime)))
    } verify { result ->
        result.assertIsEqualTo(listOf(pairOf(ted, bill)))
        execute.spyReceivedValues.getOrNull(0)
            .assertIsEqualTo(NextPlayerAction(GameSpin(listOf(), players, PairingRule.LongestTime)))
        wheel.spyReceivedValues.assertContains(listOf(bill))
    }

    @Test
    fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() = setup(object : FindNewPairsActionDispatcher {
        override val execute = stubActionExecutor(NextPlayerAction::class)
        override val wheel = StubWheel()
        val bill: Player = Player(id = "Bill")
        val ted: Player = Player(id = "Ted")
        val mozart: Player = Player(id = "Mozart")
        val players = listOf(bill, ted, mozart)
        val pairCandidateReports = listOf<PairCandidateReport?>(
            PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
            PairCandidateReport(ted, emptyList(), TimeResultValue(0)),
        )
        val history: List<PairAssignmentDocument> = emptyList()
    }) {
        execute.spyWillReturn(pairCandidateReports)
        wheel.spyWillReturn(bill)
    } exercise {
        perform(FindNewPairsAction(Game(history, players, PairingRule.LongestTime)))
    } verify { result ->
        result.assertIsEqualTo(
            listOf(pairOf(mozart, bill), pairOf(ted)),
        )
        execute.spyReceivedValues
            .assertIsEqualTo(
                listOf(
                    NextPlayerAction(GameSpin(history, players, PairingRule.LongestTime)),
                    NextPlayerAction(GameSpin(history, listOf(ted), PairingRule.LongestTime)),
                ),
            )
        wheel.spyReceivedValues
            .assertContains(listOf(bill, ted))
    }
}

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}
