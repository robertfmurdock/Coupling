package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.channels.Channel
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class FindNewPairsActionTest {

    @Test
    fun withTwoPlayersEachShouldBeRemovedFromWheelBeforeEachPlay() = asyncSetup(object :
        FindNewPairsAction.Dispatcher<NextPlayerAction.Dispatcher> {
        override val wheel = StubWheel()
        override val cannon = StubCannon<NextPlayerAction.Dispatcher>(mutableListOf(), Channel<Any>())
        val bill: Player = Player(id = "Bill", avatarType = null)
        val ted: Player = Player(id = "Ted", avatarType = null)
        val players = notEmptyListOf(bill, ted)
    }) {
        wheel.spyReturnValues.add(bill)
        cannon.immediateReturn[NextPlayerAction(GameSpin(players, listOf(), PairingRule.LongestTime))] =
            PairCandidateReport(ted, listOf(bill), TimeResultValue(0))
    } exercise {
        perform(FindNewPairsAction(Game(players, listOf(), PairingRule.LongestTime)))
    } verify { result ->
        result.assertIsEqualTo(notEmptyListOf(pairOf(ted, bill)))
        wheel.spyReceivedValues.assertContains(listOf(bill))
    }

    @Test
    fun shouldRemoveAPlayerFromTheWheelBeforeEachPlay() = asyncSetup(object :
        FindNewPairsAction.Dispatcher<NextPlayerAction.Dispatcher> {
        override val cannon = StubCannon<NextPlayerAction.Dispatcher>(mutableListOf(), Channel<Any>())
        override val wheel = StubWheel()
        val bill: Player = Player(id = "Bill", avatarType = null)
        val ted: Player = Player(id = "Ted", avatarType = null)
        val mozart: Player = Player(id = "Mozart", avatarType = null)
        val players = notEmptyListOf(bill, ted, mozart)
        val pairCandidateReports = listOf(
            PairCandidateReport(mozart, listOf(bill, ted), TimeResultValue(0)),
            PairCandidateReport(ted, emptyList(), TimeResultValue(0)),
        )
        val history: List<PairAssignmentDocument> = emptyList()
    }) {
        cannon.immediateReturn[NextPlayerAction(GameSpin(players, history, PairingRule.LongestTime))] =
            pairCandidateReports[0]
        cannon.immediateReturn[NextPlayerAction(GameSpin(notEmptyListOf(ted), history, PairingRule.LongestTime))] =
            pairCandidateReports[1]
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

class StubWheel : Wheel, Spy<List<Player>, Player> by SpyData() {
    override fun Array<Player>.spin(): Player = spyFunction(this.toList())
}
