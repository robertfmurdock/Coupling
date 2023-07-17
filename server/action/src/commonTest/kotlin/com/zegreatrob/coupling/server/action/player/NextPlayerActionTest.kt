package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.GameSpin
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairCandidateReport
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class NextPlayerActionTest : NextPlayerAction.Dispatcher {
    override val execute = stubActionExecutor(CreatePairCandidateReportsAction::class)

    private val bill = Player(id = "Bill", avatarType = null)
    private val ted = Player(id = "Ted", avatarType = null)
    private val amadeus = Player(id = "Mozart", avatarType = null)
    private val shorty = Player(id = "Napoleon", avatarType = null)

    @Test
    fun willUseHistoryToProduceSequenceInOrderOfLongestTimeSinceLastPairedToShortest() = setup(object {
        val players = notEmptyListOf(bill, ted, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val tedsPairCandidates = PairCandidateReport(ted, emptyList(), TimeResultValue(7))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))
    }) {
        execute.spyWillReturn(
            notEmptyListOf(billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { result ->
        result.assertIsEqualTo(tedsPairCandidates)
    }

    @Test
    fun aPersonWhoJustPairedHasLowerPriorityThanSomeoneWhoHasNotPairedInALongTime() = setup(object {
        val players = notEmptyListOf(bill, ted, amadeus, shorty)
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(5))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(0))
    }) {
        execute.spyWillReturn(notEmptyListOf(amadeusPairCandidates, shortyPairCandidates))
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    @Test
    fun sequenceWillBeFromLongestToShortest() = setup(object {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        val pairCandidates = notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
    }) {
        execute.spyWillReturn(pairCandidates)
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun sequenceWillPreferPlayerWhoHasNeverPaired() = setup(object {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), NeverPaired)
    }) {
        execute.spyWillReturn(
            notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() = setup(object {
        val players = notEmptyListOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(
            bill,
            listOf(Player(avatarType = null), Player(avatarType = null), Player(avatarType = null)),
            NeverPaired,
        )
        val amadeusPairCandidates = PairCandidateReport(
            amadeus,
            listOf(Player(avatarType = null)),
            NeverPaired,
        )
        val shortyPairCandidates = PairCandidateReport(
            shorty,
            listOf(Player(avatarType = null), Player(avatarType = null)),
            NeverPaired,
        )
    }) {
        execute.spyWillReturn(
            notEmptyListOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates),
        )
    } exercise {
        perform(NextPlayerAction(longestTimeSpin(players)))
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    private fun longestTimeSpin(players: NotEmptyList<Player>) = GameSpin(emptyList(), players, PairingRule.LongestTime)
}
