package com.zegreatrob.coupling.server.entity.player
import Spy
import SpyData
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.*
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class NextPlayerActionTest : NextPlayerActionDispatcher {
    override val actionDispatcher = StubCreatePairCandidateReportsActionDispatcher()

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

        init {
            actionDispatcher.spyWillReturn(listOf(
                    billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates
            ))
        }
    }) exercise {
        NextPlayerAction(longestTimeSpin(players))
                .perform()
    } verify { result ->
        result.assertIsEqualTo(tedsPairCandidates)
    }

    @Test
    fun aPersonWhoJustPairedHasLowerPriorityThanSomeoneWhoHasNotPairedInALongTime() = setup(object {
        val players = listOf(bill, ted, amadeus, shorty)
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(5))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(0))

        init {
            actionDispatcher.spyWillReturn(listOf(amadeusPairCandidates, shortyPairCandidates))
        }
    }) exercise {
        NextPlayerAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    @Test
    fun sequenceWillBeFromLongestToShortest() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        val pairCandidates = listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)

        init {
            actionDispatcher.spyWillReturn(pairCandidates)
        }
    }) exercise {
        NextPlayerAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun sequenceWillPreferPlayerWhoHasNeverPaired() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), NeverPaired)

        init {
            actionDispatcher.spyWillReturn(
                    listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
            )
        }
    }) exercise {
        NextPlayerAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, listOf(
                Player(),
                Player(),
                Player()
        ), NeverPaired)
        val amadeusPairCandidates = PairCandidateReport(amadeus, listOf(
                Player()
        ), NeverPaired)
        val shortyPairCandidates = PairCandidateReport(shorty, listOf(
                Player(), Player()
        ), NeverPaired)

        init {
            actionDispatcher.spyWillReturn(
                    listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
            )
        }
    }) exercise {
        NextPlayerAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    private fun longestTimeSpin(players: List<Player>) = GameSpin(emptyList(), players, PairingRule.LongestTime)

}

class StubCreatePairCandidateReportsActionDispatcher : CreatePairCandidateReportsActionDispatcher,
        Spy<CreatePairCandidateReportsAction, List<PairCandidateReport>> by SpyData() {
    override val actionDispatcher get() = cancel()
    override fun CreatePairCandidateReportsAction.perform() = spyFunction(this)
}
