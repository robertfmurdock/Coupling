package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*
import com.zegreatrob.coupling.server.action.stubCommandExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class NextPlayerActionTest : NextPlayerActionDispatcher {
    override val executor = stubCommandExecutor(CreatePairCandidateReportsAction::class)

    private val bill = Player(id = "Bill")
    private val ted = Player(id = "Ted")
    private val amadeus = Player(id = "Mozart")
    private val shorty = Player(id = "Napoleon")

    @Test
    fun willUseHistoryToProduceSequenceInOrderOfLongestTimeSinceLastPairedToShortest() = setup(object {
        val players = listOf(bill, ted, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val tedsPairCandidates = PairCandidateReport(ted, emptyList(), TimeResultValue(7))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))
    }) {
        executor.spyWillReturn(
            listOf(billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
                .successResult()
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { result ->
        result.assertIsEqualTo(tedsPairCandidates)
    }

    @Test
    fun aPersonWhoJustPairedHasLowerPriorityThanSomeoneWhoHasNotPairedInALongTime() = setup(object {
        val players = listOf(bill, ted, amadeus, shorty)
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(5))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(0))
    }) {
        executor.spyWillReturn(listOf(amadeusPairCandidates, shortyPairCandidates).successResult())
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    @Test
    fun sequenceWillBeFromLongestToShortest() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        val pairCandidates = listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)

    }) {
        executor.spyWillReturn(pairCandidates.successResult())
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun sequenceWillPreferPlayerWhoHasNeverPaired() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), NeverPaired)
    }) {
        executor.spyWillReturn(
            listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
                .successResult()
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(
            bill, listOf(Player(), Player(), Player()), NeverPaired
        )
        val amadeusPairCandidates = PairCandidateReport(
            amadeus, listOf(Player()), NeverPaired
        )
        val shortyPairCandidates = PairCandidateReport(
            shorty, listOf(Player(), Player()), NeverPaired
        )
    }) {
        executor.spyWillReturn(
            listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
                .successResult()
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    private fun longestTimeSpin(players: List<Player>) = GameSpin(emptyList(), players, PairingRule.LongestTime)

}
