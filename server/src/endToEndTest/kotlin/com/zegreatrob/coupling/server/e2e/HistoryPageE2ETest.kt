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
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.test.Test

class HistoryPageE2ETest {

    class WithTwoAssignments {
        companion object {
            fun testHistoryPage(test: suspend (List<PairAssignmentDocument>) -> Unit) = testAsync {
                val (_, pairAssignments) = setupProvider.await()
                test(pairAssignments)
            }

            val setupProvider by lazy {
                GlobalScope.async {
                    val tribe = tribeProvider.await()
                    val pairAssignments = pairAssignmentsProvider.await()

                    CouplingLogin.loginProvider.await()
                    HistoryPage.goTo(tribe.id)

                    tribe to pairAssignments
                }
            }

            val tribeProvider by lazy {
                GlobalScope.async {
                    val sdk = sdkProvider.await()
                    buildTribe()
                        .also { sdk.save(it) }
                }
            }

            val pairAssignmentsProvider by lazy {
                GlobalScope.async {
                    val sdk = sdkProvider.await()
                    val tribe = tribeProvider.await()

                    listOf(
                        buildPairAssignmentDocument(1, listOf(pairOf(Player(name = "Ollie"), Player(name = "Speedy")))),
                        buildPairAssignmentDocument(2, listOf(pairOf(Player(name = "Arthur"), Player(name = "Garth"))))
                    ).apply {
                        forEach { sdk.save(tribe.id.with(it)) }
                    }
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
        fun showsRecentPairings() = testHistoryPage { pairAssignments ->
            setupAsync(HistoryPage) exerciseAsync {
            } verifyAsync {
                this.pairAssignments.count().await()
                    .assertIsEqualTo(pairAssignments.size)
            }
        }

        @Test
        fun pairingCanBeDeleted() = testHistoryPage { pairAssignments ->
            setupAsync(HistoryPage) exerciseAsync {
                deleteButtons.get(0).performClick()
                browser.switchTo().alert().await()
                    .accept().await()
            } verifyAsync {
                browser.wait(
                    { this.pairAssignments.count().then { it == pairAssignments.size - 1 } },
                    2000,
                    "HistoryPageE2ETest.pairingCanBeDeleted"
                )

                this.pairAssignments.count().await()
                    .assertIsEqualTo(1)
            }
        }
    }

}

