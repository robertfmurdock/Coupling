package com.zegreatrob.coupling.e2e

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.e2e.AssignedPair.assignedPairCallSigns
import com.zegreatrob.coupling.e2e.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.TribeCard.header
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
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

            private val template = e2eSetup.extend(sharedSetup = { sdk ->
                sdk.save(tribe, players)
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) =
                template(PairAssignmentsPage, additionalSetup)

        }

        @Test
        fun showsTheTribeAndRoster() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(tribe.id)
        } verify {
            header.text()
                .assertIsEqualTo(tribe.name)
            PlayerRoster.playerElements.map { it.text() }.toList()
                .assertIsEqualTo(players.map { it.name })
        }

        @Test
        fun willLetYouAddPlayers() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            PlayerRoster.getAddPlayerButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/player/new/")
        }

        @Test
        fun willLetYouEditAnExistingPlayer() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            PlayerRoster.playerElements.first().element(PlayerCard.header.selector)
                .click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/player/${players[0].id}/")
        }

        @Test
        fun willLetYouViewHistory() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            viewHistoryButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/history/")
        }

        @Test
        fun willLetYouPrepareNewPairs() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            newPairsButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/prepare/")
        }

        @Test
        fun willLetYouGoToTheStatsPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            statisticsButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/statistics")
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            retiredPlayersButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo("/${tribe.id.value}/players/retired")
        }

    }

    class GivenCurrentSetOfPairsExists {

        companion object {
            val tribe by lazy {
                Tribe(
                    TribeId("${randomInt()}-PairAssignmentsPageE2ETest"),
                    name = "${randomInt()}-PairAssignmentsPageE2ETest"
                )
            }

            private val players by lazy {
                (1..5).map {
                    Player(
                        id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble"
                    )
                }
            }
            private val pairAssignmentDocument by lazy {
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId(""),
                    date = DateTime(year = 2015, month = 5, day = 30),
                    pairs = listOf(
                        pairOf(players[0], players[2]).withPins(emptyList()),
                        pairOf(players[4]).withPins(emptyList())
                    )
                )
            }

            private fun PinnedCouplingPair.players() = toPair().asArray().toList()

            private val unpairedPlayers = players - (pairAssignmentDocument.pairs.flatMap { it.players() })

            private val setup = e2eSetup.extend(beforeAll = {
                val sdk = sdkProvider.await()
                sdk.save(tribe)
                coroutineScope {
                    launch { players.forEach { sdk.save(tribe.id.with(it)) } }
                    launch { sdk.save(tribe.id.with(pairAssignmentDocument)) }
                }
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) =
                setup(PairAssignmentsPage, additionalSetup)
        }

        @Test
        fun willShowPlayersInCorrectPlaces() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(tribe.id)
        } verify {
            assignedPairElements.assertTheMostRecentPairsAreShown()
            PlayerRoster.playerElements.assertOnlyUnpairedPlayersAreShown()
        }

        private suspend fun WebdriverElementArray.assertTheMostRecentPairsAreShown() {
            get(0).getPairPlayerNames()
                .assertIsEqualTo(pairAssignmentDocument.pairs[0].players().map { it.name })
            get(1).getPairPlayerNames()
                .assertIsEqualTo(pairAssignmentDocument.pairs[1].players().map { it.name })
        }

        private suspend fun WebdriverElementArray.assertOnlyUnpairedPlayersAreShown() {
            map { it.text() }.toList()
                .assertIsEqualTo(unpairedPlayers.map { it.name })
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOffTheyDoNotDisplay() = currentPairAssignmentPageSetup {
            val sdk = sdkProvider.await()
            sdk.save(tribe.copy(callSignsEnabled = false))
        } exercise {
            goTo(tribe.id)
        } verify {
            assignedPairCallSigns.count()
                .assertIsEqualTo(0)
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOnTheyDisplay() = currentPairAssignmentPageSetup {
            val sdk = sdkProvider.await()
            sdk.save(tribe.copy(callSignsEnabled = true))
        } exercise {
            goTo(tribe.id)
        } verify {
            val callSigns = assignedPairCallSigns.map { it.text() }
            with(callSigns) {
                count().assertIsEqualTo(2)
                forEach {
                    it.split(" ").count()
                        .assertIsEqualTo(2)
                }
            }
        }

        private suspend fun WebdriverElement.getPairPlayerNames() = all(PlayerCard.playerLocator).map { it.text() }
    }

}
