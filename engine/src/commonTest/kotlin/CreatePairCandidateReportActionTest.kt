import kotlin.test.Test
import kotlin.test.assertTrue

class CreatePairCandidateReportActionTest {

    companion object : CreatePairCandidateReportActionDispatcher {
        override val couplingComparisionSyntax get() = VeryStrictComparisonSyntax
    }

    @Test
    fun shouldReturnNothingWhenNoPartnersAreAvailable() = setup(object  {
        val players: List<Player> = emptyList()
        val history: List<HistoryDocument> = emptyList()
    }) exercise {
        CreatePairCandidateReportAction(KtPlayer(_id = "player"), history, players)
                .perform()
    } verify {
        assertTrue(it.partners.isEmpty())
    }

    class ShouldDeterminePossiblePartnersForPlayerByChoosingPartner {
        companion object {
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

        class WhoHasNotPairedRecently {
            @Test
            fun whenThereIsClearlySomeoneWhoHasBeenTheLongest() = setup(object {
                val expectedPartner = jezebel
                val history = listOf(
                        HistoryDocument(listOf(CouplingPair.Double(bruce, selena))),
                        HistoryDocument(listOf(CouplingPair.Double(bruce, talia))),
                        HistoryDocument(listOf(CouplingPair.Double(expectedPartner, bruce)))
                )
            }) exercise {
                CreatePairCandidateReportAction(bruce, history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, listOf(expectedPartner), TimeResultValue(2)))
            }

            @Test
            fun whenThereIsOnePersonWhoHasPairedButNoOneElse() = setup(object {
                val history = listOf(
                        HistoryDocument(listOf(CouplingPair.Double(bruce, selena)))
                )
            }) exercise {
                CreatePairCandidateReportAction(bruce, history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, listOf(talia, jezebel), NeverPaired))
            }
        }
    }
}

object VeryStrictComparisonSyntax : CouplingComparisionSyntax {
    override fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair) = pair1 == pair2
}
