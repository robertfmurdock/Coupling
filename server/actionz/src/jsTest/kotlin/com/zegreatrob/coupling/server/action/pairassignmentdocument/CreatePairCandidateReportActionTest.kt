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
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlinx.datetime.Clock
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.test.assertTrue

class CreatePairCandidateReportActionTest {

    companion object :
        CreatePairCandidateReportAction.Dispatcher {
        fun pairAssignmentDocument(pairs: NotEmptyList<PinnedCouplingPair>) = PairAssignmentDocument(id = PairAssignmentDocumentId(""), date = Clock.System.now(), pairs = pairs)

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
        perform(CreatePairCandidateReportAction(defaultPlayer.copy(id = PlayerId.new()), history, players))
    } verify {
        assertTrue(it.partners.isEmpty())
    }

    @Suppress("unused")
    class ShouldDeterminePossiblePartnersForPlayerByChoosingPartner {
        companion object {
            val bruce = defaultPlayer.copy(id = PlayerId("Batman".toNotBlankString().getOrThrow()))
            val jezebel = defaultPlayer.copy(id = PlayerId("Jezebel Jett".toNotBlankString().getOrThrow()))
            val talia = defaultPlayer.copy(id = PlayerId("Talia".toNotBlankString().getOrThrow()))
            val selena = defaultPlayer.copy(id = PlayerId("Catwoman".toNotBlankString().getOrThrow()))
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
                                defaultPlayer.copy(id = PlayerId("Batgirl".toNotBlankString().getOrThrow())),
                            ),
                        ),
                    ),
                    pairAssignmentDocument(
                        notEmptyListOf(
                            pinnedPair(
                                bruce,
                                defaultPlayer.copy(id = PlayerId("Robin".toNotBlankString().getOrThrow())),
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
