package com.zegreatrob.coupling.server.e2e

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresentDuration
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
import kotlin.test.Test

class HistoryPageE2ETest {

    class Context(val pairAssignments: List<PairAssignmentDocument>) {
        val page = HistoryPage
    }

    class WithTwoAssignments {
        companion object {

            private val beforeAll by lazyDeferred {
                val sdk = sdkProvider.await()
                val tribe = buildTribe()
                sdk.save(tribe)

                val pairAssignments = setupTwoPairAssignments(tribe, sdk)

                HistoryPage.goTo(tribe.id)

                Context(pairAssignments)
            }

            private fun historyPageSetup() = e2eSetup(contextProvider = beforeAll::await)

            private suspend fun setupTwoPairAssignments(tribe: Tribe, sdk: Sdk) = listOf(
                buildPairAssignmentDocument(1, listOf(pairOf(Player(name = "Ollie"), Player(name = "Speedy")))),
                buildPairAssignmentDocument(2, listOf(pairOf(Player(name = "Arthur"), Player(name = "Garth"))))
            ).apply {
                forEach { sdk.save(tribe.id.with(it)) }
            }

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
            page.pairAssignments.count().await()
                .assertIsEqualTo(pairAssignments.size)
        }

        @Test
        fun pairingCanBeDeleted() = historyPageSetup()
            .exercise {
                page.deleteButtons.get(0).performClick()
                browser.switchTo().alert().await()
                    .accept().await()
            } verify {
            browser.wait(
                { page.pairAssignments.count().then { it == pairAssignments.size - 1 } },
                waitToBePresentDuration,
                "HistoryPageE2ETest.pairingCanBeDeleted"
            )
            page.pairAssignments.count().await()
                .assertIsEqualTo(1)
        }
    }
}
