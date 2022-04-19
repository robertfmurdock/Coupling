package com.zegreatrob.coupling.e2e.test

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairCallSigns
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.test.ConfigHeader.retiredPlayersButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.statisticsButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.viewHistoryButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.TribeCard.header
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

@Suppress("unused")
class PairAssignmentsPageE2ETest {

    companion object {
        private suspend fun Sdk.save(tribe: Party, players: List<Player>) {
            partyRepository.save(tribe)
            with(playerRepository) {
                players.forEach { save(tribe.id.with(it)) }
            }
        }
    }

    class GivenNoCurrentSetOfPairs {

        companion object {
            val tribe by lazy {
                Party(
                    PartyId("${randomInt()}-PairAssignmentsPageE2ETest"),
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
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/player/new/"))
        }

        @Test
        fun willLetYouEditAnExistingPlayer() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            PlayerRoster.playerElements.first().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/player/${players[0].id}/"))
        }

        @Test
        fun willLetYouViewHistory() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            viewHistoryButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/history/"))
        }

        @Test
        fun willLetYouPrepareNewPairs() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            newPairsButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/prepare/"))
        }

        @Test
        fun willLetYouGoToTheStatsPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            statisticsButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/statistics"))
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = currentPairAssignmentPageSetup {
            goTo(tribe.id)
        } exercise {
            retiredPlayersButton.click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/players/retired"))
        }
    }

    class GivenCurrentSetOfPairsExists {

        companion object {
            val tribe by lazy {
                Party(
                    PartyId("${randomInt()}-PairAssignmentsPageE2ETest"),
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
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(year = 2015, month = 5, day = 30),
                    pairs = listOf(
                        pairOf(players[0], players[2]).withPins(emptyList()),
                        pairOf(players[4]).withPins(emptyList())
                    )
                )
            }

            private fun PinnedCouplingPair.players() = toPair().asArray().toList()

            private val unpairedPlayers = players - (pairAssignmentDocument.pairs.flatMap { it.players() }).toSet()

            private val setup = e2eSetup.extend(beforeAll = {
                sdkProvider.await().apply {
                    tribe.save()
                    coroutineScope {
                        launch { players.forEach { tribe.id.with(it).save() } }
                        launch { sdk.pairAssignmentDocumentRepository.save(tribe.id.with(pairAssignmentDocument)) }
                    }
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
            sdkProvider.await().apply {
                tribe.copy(callSignsEnabled = false)
                    .save()
            }
        } exercise {
            goTo(tribe.id)
        } verify {
            assignedPairCallSigns.count()
                .assertIsEqualTo(0)
        }

        @Test
        fun whenTheTribeHasCallSignsTurnedOnTheyDisplay() = currentPairAssignmentPageSetup {
            sdkProvider.await().apply {
                tribe.copy(callSignsEnabled = true).save()
            }
            WelcomePage.goTo()
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

fun resolve(base: String, path: String) = if (base == "")
    "/$path"
else
    "$base/$path"

val clientBasename get() = "${process.env.CLIENT_BASENAME}".let { if (it.isNotEmpty()) "/$it" else "" }
