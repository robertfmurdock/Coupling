package com.zegreatrob.coupling.server.action.pairassignmentdocument
import Spy
import SpyData
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.test.assertEquals

class FindNewPairsActionTest {

    @Test
    fun withNoPlayersShouldReturnNoPairs() = setup(object : FindNewPairsActionDispatcher, Wheel {
        override val actionDispatcher = StubNextPlayerActionDispatcher()
        override val wheel = this
    }) exercise {
        FindNewPairsAction(Game(listOf(), listOf(), PairingRule.LongestTime))
                .perform()
    } verify { assertEquals(it, listOf()) }

    @Test
    fun withTwoPlayersEachShouldBeRemovedFromWheelBeforeEachPlay() = setup(object : FindNewPairsActionDispatcher {
        override val actionDispatcher = StubNextPlayerActionDispatcher()
        override val wheel = StubWheel()
        val bill: Player = Player(id = "Bill")
        val ted: Player = Player(id = "Ted")
        val players = listOf(bill, ted)

        init {
            wheel.spyReturnValues.add(bill)
            actionDispatcher.spyReturnValues.add(PairCandidateReport(ted, listOf(bill),
                TimeResultValue(0)
            ))
        }
    }) exercise {
        FindNewPairsAction(Game(listOf(), players, PairingRule.LongestTime))
                .perform()
    } verify { result ->
        result.assertIsEqualTo(listOf(CouplingPair.Double(ted, bill)))
        actionDispatcher.spyReceivedValues.getOrNull(0)
                .assertIsEqualTo(NextPlayerAction(GameSpin(listOf(), players, PairingRule.LongestTime)))
        wheel.spyReceivedValues.assertContains(listOf(bill))
    }

    @Test
    fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() = setup(object : FindNewPairsActionDispatcher {
        override val actionDispatcher = StubNextPlayerActionDispatcher()
        override val wheel = StubWheel()
        val bill: Player = Player(id = "Bill")
        val ted: Player = Player(id = "Ted")
        val mozart: Player =
            Player(id = "Mozart")
        val players = listOf(bill, ted, mozart)

        val pairCandidateReports = listOf(
                PairCandidateReport(mozart, listOf(bill, ted),
                    TimeResultValue(0)
                ),
                PairCandidateReport(ted, emptyList(),
                    TimeResultValue(0)
                )
        )
        val history: List<PairAssignmentDocument> = emptyList()

        init {
            actionDispatcher spyWillReturn pairCandidateReports
            wheel spyWillReturn bill
        }
    }) exercise {
        FindNewPairsAction(Game(history, players, PairingRule.LongestTime))
                .perform()
    } verify { result ->
        result.assertIsEqualTo(
                listOf(CouplingPair.Double(mozart, bill), CouplingPair.Single(ted))
        )
        actionDispatcher.spyReceivedValues
                .assertIsEqualTo(listOf(
                        NextPlayerAction(GameSpin(history, players, PairingRule.LongestTime)),
                        NextPlayerAction(GameSpin(history, listOf(ted), PairingRule.LongestTime))
                ))
        wheel.spyReceivedValues
                .assertContains(listOf(bill, ted))
    }

}

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}

class StubNextPlayerActionDispatcher : NextPlayerActionDispatcher,
        Spy<NextPlayerAction, PairCandidateReport> by SpyData() {
    override val actionDispatcher get() = throw NotImplementedError()
    override fun NextPlayerAction.perform() = spyFunction(this)
}

