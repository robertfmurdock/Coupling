import kotlin.test.Test

class GetNextPairActionTest : GetNextPairActionDispatcher {
    override val actionDispatcher = StubCreateAllPairCandidateReportsActionDispatcher()

    private val bill = KtPlayer(_id = "Bill")
    private val ted = KtPlayer(_id = "Ted")
    private val amadeus = KtPlayer(_id = "Mozart")
    private val shorty = KtPlayer(_id = "Napoleon")

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
        GetNextPairAction(longestTimeSpin(players))
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
        GetNextPairAction(longestTimeSpin(players))
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
        GetNextPairAction(longestTimeSpin(players))
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
        GetNextPairAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(shortyPairCandidates) }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() = setup(object {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, listOf(
                KtPlayer(),
                KtPlayer(),
                KtPlayer()
        ), NeverPaired)
        val amadeusPairCandidates = PairCandidateReport(amadeus, listOf(
                KtPlayer()
        ), NeverPaired)
        val shortyPairCandidates = PairCandidateReport(shorty, listOf(
                KtPlayer(), KtPlayer()
        ), NeverPaired)

        init {
            actionDispatcher.spyWillReturn(
                    listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
            )
        }
    }) exercise {
        GetNextPairAction(longestTimeSpin(players))
                .perform()
    } verify { it.assertIsEqualTo(amadeusPairCandidates) }

    private fun longestTimeSpin(players: List<KtPlayer>) = GameSpin(emptyList(), players, PairingRule.LongestTime)

}

class StubCreateAllPairCandidateReportsActionDispatcher : CreateAllPairCandidateReportsActionDispatcher,
        Spy<CreateAllPairCandidateReportsAction, List<PairCandidateReport>> by SpyData() {
    override val actionDispatcher get() = cancel()
    override fun CreateAllPairCandidateReportsAction.perform() = spyFunction(this)
}
