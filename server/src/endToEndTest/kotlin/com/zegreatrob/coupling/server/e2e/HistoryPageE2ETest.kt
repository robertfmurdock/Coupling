package com.zegreatrob.coupling.server.e2e

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresentDuration
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.test.Test

class HistoryPageE2ETest {

    class WithTwoAssignments {
        companion object {
            private fun historyPageSetup() = asyncSetupTeardown(setupHistoryPageWithPairAssignments(), { checkLogs() })

            private fun setupHistoryPageWithPairAssignments() = suspend {
                val (_, pairAssignments) = setupProvider.await()
                HistoryPageSetup(pairAssignments)
            }

            private val setupProvider by lazyDeferred {
                val tribe = tribeProvider.await()
                val pairAssignments = pairAssignmentsProvider.await()

                CouplingLogin.loginProvider.await()
                HistoryPage.goTo(tribe.id)

                tribe to pairAssignments
            }

            private val tribeProvider by lazyDeferred {
                val sdk = sdkProvider.await()
                buildTribe()
                    .also { sdk.save(it) }
            }

            private val pairAssignmentsProvider by lazyDeferred {
                val sdk = sdkProvider.await()
                val tribe = tribeProvider.await()

                listOf(
                    buildPairAssignmentDocument(1, listOf(pairOf(Player(name = "Ollie"), Player(name = "Speedy")))),
                    buildPairAssignmentDocument(2, listOf(pairOf(Player(name = "Arthur"), Player(name = "Garth"))))
                ).apply {
                    forEach { sdk.save(tribe.id.with(it)) }
                }
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

class HistoryPageSetup(val pairAssignments: List<PairAssignmentDocument>) {
    val page = HistoryPage
}

private fun <C : Any> asyncSetupTeardown(contextProvider: suspend () -> C, teardownFunc: suspend C.(Unit) -> Unit) =
    object {
        infix fun exercise(exerciseFunc: suspend C.() -> Unit) = asyncSetup(contextProvider = contextProvider)
            .exercise(exerciseFunc)
            .let { verifyObj ->
                object {
                    infix fun verify(verify: suspend C.(Unit) -> Unit) {
                        verifyObj.verifyAnd(verify) teardown teardownFunc
                    }
                }
            }
    }
