package com.zegreatrob.coupling.server.e2e

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.ElementSelector
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

@Suppress("unused")
class PairAssignmentsPageE2ETest {

    companion object {
        private suspend fun AuthorizedSdk.save(tribe: Tribe, players: List<Player>) {
            save(tribe)
            players.forEach { save(tribe.id.with(it)) }
        }
    }

    class GivenNoCurrentSetOfPairs {
        companion object {

            val tribe by lazy {
                Tribe(
                    TribeId("${randomInt()}-PairAssignmentsPageE2ETest"),
                    name = "${randomInt()}-PairAssignmentsPageE2ETest"
                )
            }

            val players by lazy {
                (1..5).map {
                    Player(
                        id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble"
                    )
                }
            }


            fun testPairAssignmentsPage(handler: suspend () -> Unit) = testAsync {
                setupProvider.await()

                try {
                    handler()
                } finally {
                    checkLogs()
                }

            }

            val setupProvider by lazyDeferred {
                sdkProvider.await().save(tribe, players)

                CouplingLogin.loginProvider.await()
            }

        }

        @Test
        fun showsTheTribeAndRoster() = testPairAssignmentsPage {
            setupAsync(object {}) exerciseAsync {
                CurrentPairAssignmentPage.goTo(tribe.id)
            } verifyAsync {
                TribeCard.header.getText().await()
                    .assertIsEqualTo(tribe.name)
                PlayerRoster.playerElements.map { it.getText() }.await().toList()
                    .assertIsEqualTo(players.map { it.name })
            }
        }

        @Test
        fun willLetYouAddPlayers() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                PlayerRoster.addPlayerButton.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/player/new/")
            }
        }

        @Test
        fun willLetYouEditAnExistingPlayer() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                PlayerRoster.playerElements.first().element(PlayerCard.headerLocator)
                    .performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/player/${players[0].id}/")
            }
        }

        @Test
        fun willLetYouViewHistory() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                viewHistoryButton.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/history/")
            }
        }

        @Test
        fun willLetYouPrepareNewPairs() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                newPairsButton.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/prepare/")
            }
        }

        @Test
        fun willLetYouGoToTheStatsPage() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                statisticsButton.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/statistics")
            }
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                goTo(tribe.id)
            } exerciseAsync {
                retiredPlayersButton.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/players/retired")
            }
        }

    }

    class GivenCurrentSetOfPairsExists {
        val tribe by lazy {
            Tribe(
                TribeId("${randomInt()}-PairAssignmentsPageE2ETest"),
                name = "${randomInt()}-PairAssignmentsPageE2ETest"
            )
        }

        val players by lazy {
            (1..5).map {
                Player(
                    id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
                    name = "player$it",
                    callSignAdjective = "nimble",
                    callSignNoun = "thimble"
                )
            }
        }
        val pairAssignmentDocument by lazy {
            PairAssignmentDocument(
                date = DateTime(year = 2015, month = 5, day = 30),
                pairs = listOf(
                    pairOf(players[0], players[2]).withPins(emptyList()),
                    pairOf(players[4]).withPins(emptyList())
                )
            )
        }

        val unpairedPlayers = players - (pairAssignmentDocument.pairs.flatMap { it.players() })

        val beforeAllProvider by lazyDeferred {
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            coroutineScope {
                launch { players.forEach { sdk.save(tribe.id.with(it)) } }
                launch { sdk.save(tribe.id.with(pairAssignmentDocument)) }
            }
            CouplingLogin.loginProvider.await()
        }

        private fun testPairAssignmentsPage(handler: suspend () -> Unit) = testAsync {
            beforeAllProvider.await()
            try {
                handler()
            } finally {
                checkLogs()
            }
        }

        @Test
        fun willShowPlayersInCorrectPlaces() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) exerciseAsync {
                goTo(tribe.id)
            } verifyAsync {
                assignedPairElements.assertTheMostRecentPairsAreShown()
                PlayerRoster.playerElements.assertOnlyUnpairedPlayersAreShown()
            }
        }

        private suspend fun ElementSelector.assertTheMostRecentPairsAreShown() {
            get(0).getPairPlayerNames()
                .assertIsEqualTo(pairAssignmentDocument.pairs[0].players().map { it.name })
            get(1).getPairPlayerNames()
                .assertIsEqualTo(pairAssignmentDocument.pairs[1].players().map { it.name })
        }

        private suspend fun ElementSelector.assertOnlyUnpairedPlayersAreShown() {
            map { it.getText() }.await().toList()
                .assertIsEqualTo(unpairedPlayers.map { it.name })
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOffTheyDoNotDisplay() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                val sdk = sdkProvider.await()
                sdk.save(tribe.copy(callSignsEnabled = false))
            } exerciseAsync {
                goTo(tribe.id)
            } verifyAsync {
                assignedPairCallSigns.count().await()
                    .assertIsEqualTo(0)
            }
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOnTheyDisplay() = testPairAssignmentsPage {
            setupAsync(CurrentPairAssignmentPage) {
                val sdk = sdkProvider.await()
                sdk.save(tribe.copy(callSignsEnabled = true))
            } exerciseAsync {
                goTo(tribe.id)
            } verifyAsync {
                val callSigns = assignedPairCallSigns.map { it.getText() }.await()
                with(callSigns) {
                    count().assertIsEqualTo(2)
                    forEach {
                        it.split(" ").count()
                            .assertIsEqualTo(2)
                    }
                }
            }
        }

        private fun PinnedCouplingPair.players() = toPair().asArray().toList()

        private suspend fun ElementSelector.getPairPlayerNames() = all(PlayerCard.playerLocator)
            .map { it.getText() }.await().toList()
    }

}