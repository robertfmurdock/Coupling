import kotlin.test.Test

class CreateAllPairCandidateReportsActionTest {

    class WhenTheTribePrefersPairingWithDifferentBadges : CreatePairCandidateReportsActionDispatcher {
        override val actionDispatcher = StubCreatePairCandidateReportActionDispatcher()

        @Test
        fun willReturnAllReportsForPlayersWithTheSameBadge() = setup(object {
            val bill = Player(_id = "Bill", badge = 1)
            val ted = Player(_id = "Ted", badge = 1)
            val amadeus = Player(_id = "Mozart", badge = 1)
            val shorty = Player(_id = "Napoleon", badge = 1)

            val players = listOf(bill, ted, amadeus, shorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(amadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(shorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            val history = emptyList<HistoryDocument>()

            init {
                expectedReports.forEach { report ->
                    actionDispatcher.givenPlayerReturnReport(report, players.without(report.player), history)
                }
            }

        }) exercise {
            CreatePairCandidateReportsAction(GameSpin(history, players, PairingRule.PreferDifferentBadge))
                    .perform()
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = setup(object {
            val history = emptyList<HistoryDocument>()
            val bill = Player(_id = "Bill", badge = 1)
            val ted = Player(_id = "Ted", badge = 1)
            val altAmadeus = Player(_id = "Mozart", badge = 2)
            val altShorty = Player(_id = "Napoleon", badge = 2)
            val players = listOf(bill, ted, altAmadeus, altShorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(altShorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            init {
                actionDispatcher.run {
                    givenPlayerReturnReport(billReport, listOf(altAmadeus, altShorty), history)
                    givenPlayerReturnReport(tedReport, listOf(altAmadeus, altShorty), history)
                    givenPlayerReturnReport(amadeusReport, listOf(bill, ted), history)
                    givenPlayerReturnReport(shortyReport, listOf(bill, ted), history)
                }
            }
        }) exercise {
            CreatePairCandidateReportsAction(GameSpin(history, players, PairingRule.PreferDifferentBadge))
                    .perform()
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnReportForOnePlayer() = setup(object {
            val history = emptyList<HistoryDocument>()
            val bill = Player(_id = "Bill", badge = 1)
            val players = listOf(bill)
            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))

            init {
                actionDispatcher.givenPlayerReturnReport(billReport, emptyList(), history)
            }

        }) exercise {
            CreatePairCandidateReportsAction(GameSpin(history, players, PairingRule.PreferDifferentBadge))
                    .perform()
        } verify {
            it.assertIsEqualTo(listOf(billReport))
        }

    }

    @Test
    fun whenTheTribePrefersPairingByLongestTime() = setup(object : CreatePairCandidateReportsActionDispatcher {
        override val actionDispatcher = StubCreatePairCandidateReportActionDispatcher()
        val history = listOf<HistoryDocument>()
        val bill = Player(_id = "Bill", badge = 1)
        val ted = Player(_id = "Ted", badge = 1)
        val altAmadeus = Player(_id = "Mozart", badge = 2)
        val altShorty = Player(_id = "Napoleon", badge = 2)
        val players = listOf(bill, ted, altAmadeus, altShorty)

        val billReport = PairCandidateReport(bill, emptyList(), NeverPaired)
        val tedReport = PairCandidateReport(ted, emptyList(), NeverPaired)
        val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), NeverPaired)
        val shortyReport = PairCandidateReport(altShorty, emptyList(), NeverPaired)
        val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

        init {
            actionDispatcher.run {
                givenPlayerReturnReport(billReport, players.without(bill), history)
                givenPlayerReturnReport(tedReport, players.without(ted), history)
                givenPlayerReturnReport(amadeusReport, players.without(altAmadeus), history)
                givenPlayerReturnReport(shortyReport, players.without(altShorty), history)
            }
        }
    }) exercise {
        CreatePairCandidateReportsAction(GameSpin(history, players, PairingRule.LongestTime))
                .perform()
    } verify {
        it.assertIsEqualTo(expectedReports)
    }

    companion object {

        private fun StubCreatePairCandidateReportActionDispatcher.givenPlayerReturnReport(
                pairCandidateReport: PairCandidateReport,
                players: List<Player>,
                history: List<HistoryDocument>
        ) = whenever(
                receive = expectedAction(pairCandidateReport.player, history, players),
                returnValue = pairCandidateReport
        )

        private fun expectedAction(player: Player, history: List<HistoryDocument>, players: List<Player>) =
                CreatePairCandidateReportAction(
                        player,
                        history,
                        players
                )

        private fun List<Player>.without(player: Player) = filterNot { it == player }
    }

}

class StubCreatePairCandidateReportActionDispatcher :
        CreatePairCandidateReportActionDispatcher, Spy<CreatePairCandidateReportAction, PairCandidateReport>
by SpyData() {
    override fun CreatePairCandidateReportAction.perform() = spyFunction(this)
}