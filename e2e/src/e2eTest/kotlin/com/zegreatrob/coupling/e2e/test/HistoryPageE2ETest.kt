package com.zegreatrob.coupling.e2e.test

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
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
                val sdk = sdkProvider.await().apply {
                    party.save()
                }

                val pairAssignments = setupTwoPairAssignments(party, sdk)

                HistoryPage.goTo(party.id)
                Context(pairAssignments)
            })

            private suspend fun setupTwoPairAssignments(tribe: Party, sdk: Sdk) = listOf(
                buildPairAssignmentDocument(1, listOf(pairOf(Player(name = "Ollie"), Player(name = "Speedy")))),
                buildPairAssignmentDocument(2, listOf(pairOf(Player(name = "Arthur"), Player(name = "Garth"))))
            ).onEach { sdk.pairAssignmentDocumentRepository.save(tribe.id.with(it)) }

            private fun buildPairAssignmentDocument(number: Int, pairs: List<CouplingPair>) = PairAssignmentDocument(
                PairAssignmentDocumentId("${DateTime.now().milliseconds}-HistoryPageE2ETest-$number"),
                DateTime.now(),
                pairs.map { it.withPins(emptySet()) }
            )

            private fun buildParty() = "${randomInt()}-HistoryPageE2ETest".let {
                Party(it.let(::PartyId), name = it)
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
            HistoryPage.deleteButtons.get(0).click()
            WebdriverBrowser.waitForAlert()
            WebdriverBrowser.acceptAlert()
        } verify {
            WebdriverBrowser.waitUntil(
                { HistoryPage.pairAssignments.count() == pairAssignments.size - 1 },
                waitToBePresentDuration,
                "HistoryPageE2ETest.pairingCanBeDeleted"
            )
            HistoryPage.pairAssignments.count()
                .assertIsEqualTo(1)
        }
    }
}
