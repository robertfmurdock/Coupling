import kotlin.test.Test

class CreateAllPairCandidateReportsActionTest {

    class WhenTheTribePrefersPairingWithDifferentBadges : CreateAllPairCandidateReportsActionDispatcher {
        override val actionDispatcher = StubCreatePairCandidateReportActionDispatcher()

        @Test
        fun willReturnAllReportsForPlayersWithTheSameBadge() = setup(object {
            val bill = KtPlayer(_id = "Bill", badge = "1")
            val ted = KtPlayer(_id = "Ted", badge = "1")
            val amadeus = KtPlayer(_id = "Mozart", badge = "1")
            val shorty = KtPlayer(_id = "Napoleon", badge = "1")

            val players = listOf(bill, ted, amadeus, shorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(amadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(shorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            val history = emptyList<HistoryDocument>()

            init {
                expectedReports.forEach { report ->
                    actionDispatcher.givenPlayerReturnReport(report, history, players.filterNot { it == report.player })
                }
            }
        }) exercise {
            CreateAllPairCandidateReportsAction(GameSpin(history, players, PairingRule.PreferDifferentBadge))
                    .perform()
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = setup(object {
            val history = emptyList<HistoryDocument>()
            val bill = KtPlayer(_id = "Bill", badge = "1")
            val ted = KtPlayer(_id = "Ted", badge = "1")
            val altAmadeus = KtPlayer(_id = "Mozart", badge = "2")
            val altShorty = KtPlayer(_id = "Napoleon", badge = "2")
            val players = listOf(bill, ted, altAmadeus, altShorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(altShorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            init {
                actionDispatcher.run {
                    givenPlayerReturnReport(billReport, history, listOf(altAmadeus, altShorty))
                    givenPlayerReturnReport(tedReport, history, listOf(altAmadeus, altShorty))
                    givenPlayerReturnReport(amadeusReport, history, listOf(bill, ted))
                    givenPlayerReturnReport(shortyReport, history, listOf(bill, ted))
                }
            }
        }) exercise {
            CreateAllPairCandidateReportsAction(GameSpin(history, players, PairingRule.PreferDifferentBadge))
                    .perform()
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }
    }

    companion object {

        private fun StubCreatePairCandidateReportActionDispatcher.givenPlayerReturnReport(
                pairCandidateReport: PairCandidateReport,
                history: List<HistoryDocument>,
                players: List<KtPlayer>
        ) = whenever(
                receive = expectedAction(pairCandidateReport.player, history, players),
                returnValue = pairCandidateReport
        )

        private fun expectedAction(player: Player, history: List<HistoryDocument>, players: List<KtPlayer>) =
                CreatePairCandidateReportAction(
                        player,
                        history,
                        players
                )
    }

}

class StubCreatePairCandidateReportActionDispatcher :
        CreatePairCandidateReportActionDispatcher, Spy<CreatePairCandidateReportAction, PairCandidateReport>
by SpyData() {
    override val couplingComparisionSyntax get() = cancel()

    override fun CreatePairCandidateReportAction.perform() = spyFunction(this)

}