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
import com.zegreatrob.testmints.async.Exercise
import com.zegreatrob.testmints.async.Setup
import com.zegreatrob.testmints.async.asyncSetup
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

            private val setupProvider by lazyDeferred {
                sdkProvider.await().save(tribe, players)

                CouplingLogin.loginProvider.await()
            }

        }

        @Test
        fun showsTheTribeAndRoster() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(tribe.id)
        } verify {
            TribeCard.header.getText().await()
                .assertIsEqualTo(tribe.name)
            PlayerRoster.playerElements.map { it.getText() }.await().toList()
                .assertIsEqualTo(players.map { it.name })
        }

        @Test
        fun willLetYouAddPlayers() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            PlayerRoster.addPlayerButton.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/player/new/")
        }

        private fun currentPairAssignmentPageSetup(
            setupFunc: suspend CurrentPairAssignmentPage.() -> Unit
        ) = PageSetup(asyncSetup(CurrentPairAssignmentPage) {
            setupProvider.await()
            setupFunc()
        })

        @Test
        fun willLetYouEditAnExistingPlayer() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            PlayerRoster.playerElements.first().element(PlayerCard.headerLocator)
                .performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/player/${players[0].id}/")
        }

        @Test
        fun willLetYouViewHistory() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            viewHistoryButton.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/history/")
        }

        @Test
        fun willLetYouPrepareNewPairs() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            newPairsButton.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/prepare/")
        }

        @Test
        fun willLetYouGoToTheStatsPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            statisticsButton.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/statistics")
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            retiredPlayersButton.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/players/retired")
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

        private val unpairedPlayers = players - (pairAssignmentDocument.pairs.flatMap { it.players() })

        private val beforeAllProvider by lazyDeferred {
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            coroutineScope {
                launch { players.forEach { sdk.save(tribe.id.with(it)) } }
                launch { sdk.save(tribe.id.with(pairAssignmentDocument)) }
            }
            CouplingLogin.loginProvider.await()
        }

        private fun currentPairAssignmentPageSetup(
            setupFunc: suspend CurrentPairAssignmentPage.() -> Unit
        ) = PageSetup(asyncSetup(CurrentPairAssignmentPage) {
            beforeAllProvider.await()
            setupFunc()
        })

        private fun testPairAssignmentsPage(handler: suspend () -> Unit) = testAsync {
            beforeAllProvider.await()
            try {
                handler()
            } finally {
                checkLogs()
            }
        }

        @Test
        fun willShowPlayersInCorrectPlaces() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(tribe.id)
        } verify {
            assignedPairElements.assertTheMostRecentPairsAreShown()
            PlayerRoster.playerElements.assertOnlyUnpairedPlayersAreShown()
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
        fun whenTheTribeHasCallSignsTurnedOffTheyDoNotDisplay() = currentPairAssignmentPageSetup {
            val sdk = sdkProvider.await()
            sdk.save(tribe.copy(callSignsEnabled = false))
        } exercise {
            goTo(tribe.id)
        } verify {
            assignedPairCallSigns.count().await()
                .assertIsEqualTo(0)
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOnTheyDisplay() = currentPairAssignmentPageSetup {
            val sdk = sdkProvider.await()
            sdk.save(tribe.copy(callSignsEnabled = true))
        } exercise {
            goTo(tribe.id)
        } verify {
            val callSigns = assignedPairCallSigns.map { it.getText() }.await()
            with(callSigns) {
                count().assertIsEqualTo(2)
                forEach {
                    it.split(" ").count()
                        .assertIsEqualTo(2)
                }
            }
        }

        private fun PinnedCouplingPair.players() = toPair().asArray().toList()

        private suspend fun ElementSelector.getPairPlayerNames() = all(PlayerCard.playerLocator)
            .map { it.getText() }.await().toList()
    }

}

class PageSetup<C : Any>(private val setup: Setup<C>) {
    infix fun exercise(exerciseFunc: suspend C.() -> Unit) =
        PageExercise(setup.exercise(exerciseFunc))
}

class PageExercise<C : Any>(private val exercise: Exercise<C, Unit>) {
    infix fun verify(verifyFunc: suspend C.(Unit) -> Unit) = exercise
        .verifyAnd(verifyFunc)
        .teardown { checkLogs() }
}