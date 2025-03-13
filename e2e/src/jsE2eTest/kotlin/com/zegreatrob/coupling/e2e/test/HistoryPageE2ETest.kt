package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.webdriverio.WAIT_TO_BE_PRESENT_DURATION
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.datetime.Clock
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

@Suppress("unused")
class HistoryPageE2ETest {

    class Context(val pairAssignments: List<PairAssignmentDocument>) {
        val page = HistoryPage
    }

    class WithTwoAssignments {
        companion object {
            private val historyPageSetup = e2eSetup.extend(beforeAll = {
                val party = buildParty()
                val sdk = sdk.await().apply { fire(SavePartyCommand(party)) }

                val pairAssignments = setupTwoPairAssignments(party, sdk)

                HistoryPage.goTo(party.id)
                Context(pairAssignments)
            })

            private suspend fun setupTwoPairAssignments(
                party: PartyDetails,
                sdk: ActionCannon<CouplingSdkDispatcher>,
            ) = listOf(
                buildPairAssignmentDocument(
                    1,
                    notEmptyListOf(
                        pairOf(
                            defaultPlayer.copy(name = "Ollie"),
                            defaultPlayer.copy(name = "Speedy"),
                        ),
                    ),
                ),
                buildPairAssignmentDocument(
                    2,
                    notEmptyListOf(
                        pairOf(
                            defaultPlayer.copy(name = "Arthur"),
                            defaultPlayer.copy(name = "Garth"),
                        ),
                    ),
                ),
            ).onEach { sdk.fire(SavePairAssignmentsCommand(party.id, it)) }

            private fun buildPairAssignmentDocument(number: Int, pairs: NotEmptyList<CouplingPair>) = PairAssignmentDocument(
                PairAssignmentDocumentId.new(),
                Clock.System.now(),
                pairs.map { it.withPins(emptySet()) },
            )

            private fun buildParty() = "${randomInt()}-HistoryPageE2ETest".let {
                PartyDetails(it.let(::PartyId), name = it)
            }
        }

        @Test
        fun showsRecentPairings() = historyPageSetup().exercise {
        } verify {
            HistoryPage.pairAssignments.count()
                .assertIsEqualTo(pairAssignments.size)
        }

        @Test
        fun pairingCanBeDeleted() = historyPageSetup().exercise {
            HistoryPage.getDeleteButtons()[0].click()
            WebdriverBrowser.waitForAlert()
            WebdriverBrowser.acceptAlert()
        } verify {
            WebdriverBrowser.waitUntil(
                { HistoryPage.pairAssignments.count() == pairAssignments.size - 1 },
                WAIT_TO_BE_PRESENT_DURATION,
                "HistoryPageE2ETest.pairingCanBeDeleted",
            )
            HistoryPage.pairAssignments.count()
                .assertIsEqualTo(1)
        }
    }
}
