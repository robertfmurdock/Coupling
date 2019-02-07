import com.soywiz.klock.DateTime
import kotlin.test.Test
import kotlin.test.assertTrue


class CreatePairCandidateReportActionTest {

    companion object : CreatePairCandidateReportActionDispatcher {
        fun pairAssignmentDocument(pairs: List<PinnedCouplingPair>) = PairAssignmentDocument(DateTime.now(), pairs, "", null)
        fun pinnedPair(player1: Player, player2: Player) =
                PinnedCouplingPair(listOf(player1.withPins(emptyList()), player2.withPins(emptyList())))
    }

    @Test
    fun shouldReturnNothingWhenNoPartnersAreAvailable() = setup(object {
        val players: List<Player> = emptyList()
        val history: List<PairAssignmentDocument> = emptyList()
    }) exercise {
        CreatePairCandidateReportAction(Player(id = "player"), history, players)
                .perform()
    } verify {
        assertTrue(it.partners.isEmpty())
    }

    @Suppress("unused")
    class ShouldDeterminePossiblePartnersForPlayerByChoosingPartner {
        companion object {
            val bruce = Player(id = "Batman")
            val jezebel = Player(id = "Jezebel Jett")
            val talia = Player(id = "Talia")
            val selena = Player(id = "Catwoman")
            val availableOtherPlayers = listOf(selena, talia, jezebel)

            private fun createPairCandidateReportAction(history: List<PairAssignmentDocument>, availablePlayers: List<Player>) =
                    CreatePairCandidateReportAction(bruce, history, availablePlayers)
        }

        class WhoHasNeverPaired {
            @Test
            fun withNoHistory() = setup(object {
                val history = emptyList<PairAssignmentDocument>()
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun withHistoryDocumentThatHasNoPairs() = setup(object {
                val history = listOf(pairAssignmentDocument(emptyList()))
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun withPlentyOfHistory() = setup(object {
                val history = listOf(
                        pairAssignmentDocument(listOf(pinnedPair(bruce, Player(id = "Batgirl")))),
                        pairAssignmentDocument(listOf(pinnedPair(bruce, Player(id = "Robin"))))
                )
            }) exercise {
                createPairCandidateReportAction(history, availableOtherPlayers)
                        .perform()
            } verify {
                it.assertIsEqualTo(PairCandidateReport(bruce, availableOtherPlayers, NeverPaired))
            }

            @Test
            fun onlyThePersonYouWereWithLastTime() = setup(object {
                val history = listOf(pairAssignmentDocument(listOf(
                        pinnedPair(bruce, selena)
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
                        pairAssignmentDocument(listOf(pinnedPair(bruce, selena))),
                        pairAssignmentDocument(listOf(pinnedPair(bruce, talia))),
                        pairAssignmentDocument(listOf(pinnedPair(expectedPartner, bruce)))
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
                        pairAssignmentDocument(listOf(pinnedPair(bruce, selena)))
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
