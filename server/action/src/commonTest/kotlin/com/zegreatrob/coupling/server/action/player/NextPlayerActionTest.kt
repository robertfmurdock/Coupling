package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListActionWrapper
import com.zegreatrob.coupling.server.action.pairassignmentdocument.GameSpin
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairCandidateReport
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class NextPlayerActionTest {

    private val bill = defaultPlayer.copy(id = "Bill")
    private val ted = defaultPlayer.copy(id = "Ted")
    private val amadeus = defaultPlayer.copy(id = "Mozart")
    private val shorty = defaultPlayer.copy(id = "Napoleon")

    interface NextPlayerActionTestDispatcher :
        NextPlayerAction.Dispatcher<NextPlayerActionTestDispatcher>,
        CreatePairCandidateReportListAction.Dispatcher<NextPlayerActionTestDispatcher>,
        CreatePairCandidateReportAction.Dispatcher

    @Test
    fun willUseHistoryToProduceSequenceInOrderOfLongestTimeSinceLastPairedToShortest() = asyncSetup(object :
        NextPlayerActionTestDispatcher, ScopeMint() {
        val players = notEmptyListOf(bill, ted, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val tedsPairCandidates = PairCandidateReport(ted, emptyList(), TimeResultValue(7))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))
        override val cannon = StubCannon.Synchronous<NextPlayerActionTestDispatcher>().apply {
            givenAny(
                CreatePairCandidateReportListActionWrapper::class,
                notEmptyListOf(
                    billsPairCandidates,
                    tedsPairCandidates,
                    amadeusPairCandidates,
                    shortyPairCandidates,
                ),
            )
        }
    }) exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { result ->
        result.assertIsEqualTo(tedsPairCandidates)
    }

    @Test
    fun aPersonWhoJustPairedHasLowerPriorityThanSomeoneWhoHasNotPairedInALongTime() = asyncSetup(object :
        NextPlayerAction.Dispatcher<NextPlayerActionTestDispatcher>, ScopeMint() {
        val players = notEmptyListOf(bill, ted, amadeus, shorty)
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(5))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(0))
        override val cannon = StubCannon.Synchronous<NextPlayerActionTestDispatcher>().apply {
            givenAny(
                CreatePairCandidateReportListActionWrapper::class,
                notEmptyListOf(amadeusPairCandidates, shortyPairCandidates),
            )
        }
    }) exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    @Test
    fun sequenceWillBeFromLongestToShortest() = asyncSetup(object :
        NextPlayerAction.Dispatcher<NextPlayerActionTestDispatcher>, ScopeMint() {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        override val cannon = StubCannon.Synchronous<NextPlayerActionTestDispatcher>().apply {
            givenAny(
                CreatePairCandidateReportListActionWrapper::class,
                notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
            )
        }
    }) exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun sequenceWillPreferPlayerWhoHasNeverPaired() = asyncSetup(object :
        NextPlayerAction.Dispatcher<NextPlayerActionTestDispatcher>, ScopeMint() {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), NeverPaired)
        override val cannon = StubCannon.Synchronous<NextPlayerActionTestDispatcher>().apply {
            givenAny(
                CreatePairCandidateReportListActionWrapper::class,
                notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
            )
        }
    }) exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() = asyncSetup(object :
        NextPlayerAction.Dispatcher<NextPlayerActionTestDispatcher>, ScopeMint() {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(
            bill,
            listOf(stubPlayer(), stubPlayer(), stubPlayer()),
            NeverPaired,
        )
        val amadeusPairCandidates = PairCandidateReport(
            amadeus,
            listOf(stubPlayer()),
            NeverPaired,
        )
        val shortyPairCandidates = PairCandidateReport(
            shorty,
            listOf(stubPlayer(), stubPlayer()),
            NeverPaired,
        )
        override val cannon = StubCannon.Synchronous<NextPlayerActionTestDispatcher>().apply {
            givenAny(
                CreatePairCandidateReportListActionWrapper::class,
                notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
            )
        }
    }) exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    private fun longestTimeSpin(players: NotEmptyList<Player>) = GameSpin(players, emptyList(), PairingRule.LongestTime)
}
