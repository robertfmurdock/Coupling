package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Game
import com.zegreatrob.coupling.action.pairassignmentdocument.GameSpin
import com.zegreatrob.coupling.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.action.pairassignmentdocument.PairCandidateReport
import com.zegreatrob.coupling.action.pairassignmentdocument.Wheel
import com.zegreatrob.coupling.action.pairassignmentdocument.call
import com.zegreatrob.coupling.action.pairassignmentdocument.let
import com.zegreatrob.coupling.action.pairassignmentdocument.wrap
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class FindNewPairsActionTest {

    interface FindNewPairsActionTestDispatcher :
        NextPlayerAction.Dispatcher<FindNewPairsActionTestDispatcher>,
        CreatePairCandidateReportListAction.Dispatcher<FindNewPairsActionTestDispatcher>,
        CreatePairCandidateReportAction.Dispatcher

    @Test
    fun withTwoPlayersEachShouldBeRemovedFromWheelBeforeEachPlay() = asyncSetup(object :
        FindNewPairsAction.Dispatcher<FindNewPairsActionTestDispatcher> {
        override val wheel = StubWheel()
        override val cannon = StubCannon<FindNewPairsActionTestDispatcher>(mutableListOf())
        val bill: Player = stubPlayer().copy(name = "Bill")
        val ted: Player = stubPlayer().copy(name = "Ted")
        val players = notEmptyListOf(bill, ted)
    }) {
        wheel.spyReturnValues.add(bill)
        cannon.given(
            action = NextPlayerAction(GameSpin(players, listOf(), PairingRule.LongestTime)).wrap(),
            returnValue = PairCandidateReport(ted, listOf(bill), TimeResultValue(0)),
        )
    } exercise {
        perform(FindNewPairsAction(Game(players, listOf(), PairingRule.LongestTime)))
    } verify { result ->
        result.assertIsEqualTo(notEmptyListOf(pairOf(ted, bill)))
        wheel.spyReceivedValues.assertContains(listOf(bill))
    }

    @Test
    fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() = asyncSetup(object :
        FindNewPairsAction.Dispatcher<FindNewPairsActionTestDispatcher> {
        override val cannon = StubCannon<FindNewPairsActionTestDispatcher>(mutableListOf())
        override val wheel = StubWheel()
        val bill: Player = stubPlayer().copy(name = "Bill")
        val ted: Player = stubPlayer().copy(name = "Ted")
        val mozart: Player = stubPlayer().copy(name = "Mozart")
        val players = notEmptyListOf(bill, ted, mozart)
        val pairCandidateReports = listOf(
            PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
            PairCandidateReport(ted, emptyList(), TimeResultValue(0)),
        )
        val history: List<PairingSet> = emptyList()
    }) {
        NextPlayerAction(GameSpin(players, history, PairingRule.LongestTime))
            .let(cannon::given)
            .thenReturn(pairCandidateReports[0])
        call(
            function = cannon::given,
            action = NextPlayerAction(GameSpin(notEmptyListOf(ted), history, PairingRule.LongestTime)),
        ).thenReturn(pairCandidateReports[1])

        wheel.spyWillReturn(bill)
    } exercise {
        perform(FindNewPairsAction(Game(players, history, PairingRule.LongestTime)))
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(pairOf(mozart, bill), pairOf(ted)),
        )
        wheel.spyReceivedValues
            .assertContains(listOf(bill, ted))
    }
}

class StubWheel :
    Wheel,
    Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}
