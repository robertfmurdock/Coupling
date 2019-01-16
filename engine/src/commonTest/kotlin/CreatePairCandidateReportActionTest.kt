import kotlin.test.Test
import kotlin.test.assertTrue

class CreatePairCandidateReportActionTest {

    @Test
    fun shouldReturnNothingWhenNoPartnersAreAvailable() = setup(object : CreatePairCandidateReportActionDispatcher {
        override val couplingComparisionSyntax get() = VeryStrictComparisonSyntax
        val players: List<Player> = emptyList()
        val history: List<HistoryDocument> = emptyList()
    }) exercise {
        CreatePairCandidateReportAction(KtPlayer(_id = "player"), history, players)
                .perform()
    } verify {
        assertTrue(it.partners.isEmpty())
    }

    class ShouldDeterminePossiblePartnersForPlayerByChoosingPartner {
        companion object : CreatePairCandidateReportActionDispatcher {
            override val couplingComparisionSyntax get() = VeryStrictComparisonSyntax
            val bruce = KtPlayer(_id = "Batman")
            val jezebel = KtPlayer(_id = "Jezebel Jett")
            val talia = KtPlayer(_id = "Talia")
            val selena = KtPlayer(_id = "Catwoman")
            val availableOtherPlayers = listOf(selena, talia, jezebel)

            private fun createPairCandidateReportAction(history: List<HistoryDocument>, availablePlayers: List<KtPlayer>) =
                    CreatePairCandidateReportAction(bruce, history, availablePlayers)
        }

        class WhoHasNeverPaired {
            @Test
            fun withNoHistory() = setup(object {
                val history = emptyList<HistoryDocument>()
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun withHistoryDocumentThatHasNoPairs() = setup(object {
                val history = listOf(HistoryDocument(emptyList()))
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun withPlentyOfHistory() = setup(object {
                val history = listOf(
                        HistoryDocument(listOf(CouplingPair.Double(bruce, KtPlayer(_id = "Batgirl")))),
                        HistoryDocument(listOf(CouplingPair.Double(bruce, KtPlayer(_id = "Robin"))))
                )
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun onlyThePersonYouWereWithLastTime() = setup(object {
                val history = listOf(HistoryDocument(listOf(
                        CouplingPair.Double(bruce, selena)
                )))
            }) exercise {
                CreatePairCandidateReportAction(bruce, history, listOf(selena))
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, listOf(selena), TimeResultValue(0)))
            }
        }


    }

}

object VeryStrictComparisonSyntax : CouplingComparisionSyntax {
    override fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair) = pair1 == pair2
}
