package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.PairCandidateReport
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock

class CreatePairCandidateReportActionTest {

    companion object :
        CreatePairCandidateReportAction.Dispatcher {
        fun pairAssignmentDocument(pairs: NotEmptyList<PinnedCouplingPair>) = PairAssignmentDocument(id = PairAssignmentDocumentId.new(), date = Clock.System.now(), pairs = pairs)

        fun pinnedPair(player1: Player, player2: Player) = PinnedCouplingPair(
            notEmptyListOf(
                player1.withPins(emptyList()),
                player2.withPins(emptyList()),
            ),
        )
    }

    @Test
    fun shouldReturnNothingWhenNoPartnersAreAvailable() = setup(object {
        val players: List<Player> = emptyList()
        val history: List<PairAssignmentDocument> = emptyList()
    }) exercise {
        perform(CreatePairCandidateReportAction(stubPlayer(), history, players))
    } verify {
        assertTrue(it.partners.isEmpty())
    }

    @Suppress("unused")
    class ShouldDeterminePossiblePartnersForPlayerByChoosingPartner {
        companion object {
            val bruce = stubPlayer().copy(name = "Batman")
            val jezebel = stubPlayer().copy(name = "Jezebel Jett")
            val talia = stubPlayer().copy(name = "Talia")
            val selena = stubPlayer().copy(name = "Catwoman")
            val availableOtherPlayers = listOf(selena, talia, jezebel)

            private fun createPairCandidateReportAction(
                history: List<PairAssignmentDocument>,
                availablePlayers: List<Player>,
            ) = CreatePairCandidateReportAction(bruce, history, availablePlayers)
        }

        class WhoHasNeverPaired {
            @Test
            fun withNoHistory() = setup(object {
                val history = emptyList<PairAssignmentDocument>()
            }) exercise {
                perform(createPairCandidateReportAction(history, availableOtherPlayers))
            } verify {
                it.assertIsEqualTo(
                    PairCandidateReport(
                        bruce,
                        availableOtherPlayers,
                        NeverPaired,
                    ),
                )
            }

            @Test
            fun withPlentyOfHistory() = setup(object {
                val history = listOf(
                    pairAssignmentDocument(
                        notEmptyListOf(
                            pinnedPair(
                                bruce,
                                stubPlayer().copy(name = "Batgirl"),
                            ),
                        ),
                    ),
                    pairAssignmentDocument(
                        notEmptyListOf(
                            pinnedPair(
                                bruce,
                                stubPlayer().copy(name = "Robin"),
                            ),
                        ),
                    ),
                )
            }) exercise {
                perform(createPairCandidateReportAction(history, availableOtherPlayers))
            } verify {
                it.assertIsEqualTo(
                    PairCandidateReport(
                        bruce,
                        availableOtherPlayers,
                        NeverPaired,
                    ),
                )
            }

            @Test
            fun onlyThePersonYouWereWithLastTime() = setup(object {
                val history = listOf(
                    pairAssignmentDocument(
                        notEmptyListOf(
                            pinnedPair(bruce, selena),
                        ),
                    ),
                )
            }) exercise {
                perform(CreatePairCandidateReportAction(bruce, history, listOf(selena)))
            } verify {
                it.assertIsEqualTo(
                    PairCandidateReport(
                        bruce,
                        listOf(selena),
                        TimeResultValue(0),
                    ),
                )
            }
        }

        class WhoHasNotPairedRecently {
            @Test
            fun whenThereIsClearlySomeoneWhoHasBeenTheLongest() = setup(object {
                val expectedPartner = jezebel
                val history = listOf(
                    pairAssignmentDocument(notEmptyListOf(pinnedPair(bruce, selena))),
                    pairAssignmentDocument(notEmptyListOf(pinnedPair(bruce, talia))),
                    pairAssignmentDocument(notEmptyListOf(pinnedPair(expectedPartner, bruce))),
                )
            }) exercise {
                perform(CreatePairCandidateReportAction(bruce, history, availableOtherPlayers))
            } verify {
                it.assertIsEqualTo(
                    PairCandidateReport(
                        bruce,
                        listOf(expectedPartner),
                        TimeResultValue(2),
                    ),
                )
            }

            @Test
            fun whenThereIsOnePersonWhoHasPairedButNoOneElse() = setup(object {
                val history = listOf(
                    pairAssignmentDocument(notEmptyListOf(pinnedPair(bruce, selena))),
                )
            }) exercise {
                perform(CreatePairCandidateReportAction(bruce, history, availableOtherPlayers))
            } verify {
                it.assertIsEqualTo(
                    PairCandidateReport(
                        bruce,
                        listOf(talia, jezebel),
                        NeverPaired,
                    ),
                )
            }
        }
    }
}
