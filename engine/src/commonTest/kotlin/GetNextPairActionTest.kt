import kotlin.test.Test

class GetNextPairActionTest : GetNextPairActionDispatcher {
    override val actionDispatcher = StubCreateAllPairCandidateReportsActionDispatcher()

    val bill = KtPlayer(_id = "Bill")
    val ted = KtPlayer(_id = "Ted")
    val amadeus = KtPlayer(_id = "Mozart")
    val shorty = KtPlayer(_id = "Napoleon")

    @Test
    fun willUseHistoryToProduceSequenceInOrderOfLongestTimeSinceLastPairedToShortest() {
        val players = listOf(bill, ted, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val tedsPairCandidates = PairCandidateReport(ted, emptyList(), TimeResultValue(7))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        actionDispatcher.spyWillReturn(listOf(
                billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates
        ))

        GetNextPairAction(longestTimeSpin(players))
                .perform()
                .assertIsEqualTo(tedsPairCandidates)
    }

    @Test
    fun aPersonWhoJustPairedHasLowerPriorityThanSomeoneWhoHasNotPairedInALongTime() {
        val players = listOf(bill, ted, amadeus, shorty)

        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(5))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(0))

        actionDispatcher.spyWillReturn(
                listOf(amadeusPairCandidates, shortyPairCandidates)
        )

        GetNextPairAction(longestTimeSpin(players))
                .perform()
                .assertIsEqualTo(amadeusPairCandidates)
    }

    @Test
    fun sequenceWillBeFromLongestToShortest() {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), TimeResultValue(5))

        actionDispatcher.spyWillReturn(
                listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
        )

        GetNextPairAction(longestTimeSpin(players))
                .perform()
                .assertIsEqualTo(shortyPairCandidates)
    }

    @Test
    fun sequenceWillPreferPlayerWhoHasNeverPaired() {
        val players = listOf(bill, amadeus, shorty)

        val billsPairCandidates = PairCandidateReport(bill, emptyList(), TimeResultValue(3))
        val amadeusPairCandidates = PairCandidateReport(amadeus, emptyList(), TimeResultValue(4))
        val shortyPairCandidates = PairCandidateReport(shorty, emptyList(), NeverPaired)

        actionDispatcher.spyWillReturn(
                listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
        )

        GetNextPairAction(longestTimeSpin(players))
                .perform()
                .assertIsEqualTo(shortyPairCandidates)
    }

    @Test
    fun willPrioritizeTheReportWithFewestPlayersGivenEqualAmountsOfTime() {
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

        actionDispatcher.spyWillReturn(
                listOf(billsPairCandidates, amadeusPairCandidates, shortyPairCandidates)
        )

        GetNextPairAction(longestTimeSpin(players))
                .perform()
                .assertIsEqualTo(amadeusPairCandidates)
    }

    private fun longestTimeSpin(players: List<KtPlayer>) = GameSpin(emptyList(), players, PairingRule.LongestTime)

}

class StubCreateAllPairCandidateReportsActionDispatcher : CreateAllPairCandidateReportsActionDispatcher,
        Spy<CreateAllPairCandidateReportsAction, List<PairCandidateReport>> by SpyData() {
    override val actionDispatcher get() = cancel()
    override fun CreateAllPairCandidateReportsAction.perform() = spyFunction(this)
}
