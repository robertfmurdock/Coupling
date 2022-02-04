package com.zegreatrob.coupling.e2e.test

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class HistoryPageE2ETest {

    class Context(val pairAssignments: List<PairAssignmentDocument>) {
        val page = HistoryPage
    }

    class WithTwoAssignments {
        companion object {
            private val historyPageSetup = e2eSetup.extend(beforeAll = {
                val sdk = sdkProvider.await()
                val tribe = buildTribe()
                sdk.save(tribe)

                val pairAssignments = setupTwoPairAssignments(tribe, sdk)

                HistoryPage.goTo(tribe.id)
                Context(pairAssignments)
            })

            private suspend fun setupTwoPairAssignments(tribe: Tribe, sdk: Sdk) = listOf(
                buildPairAssignmentDocument(1, listOf(pairOf(Player(name = "Ollie"), Player(name = "Speedy")))),
                buildPairAssignmentDocument(2, listOf(pairOf(Player(name = "Arthur"), Player(name = "Garth"))))
            ).onEach { sdk.save(tribe.id.with(it)) }

            private fun buildPairAssignmentDocument(number: Int, pairs: List<CouplingPair>) = PairAssignmentDocument(
                PairAssignmentDocumentId("${DateTime.now().milliseconds}-HistoryPageE2ETest-$number"),
                DateTime.now(),
                pairs.map { it.withPins(emptyList()) }
            )

            private fun buildTribe() = "${randomInt()}-HistoryPageE2ETest".let {
                Tribe(it.let(::TribeId), name = it)
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
