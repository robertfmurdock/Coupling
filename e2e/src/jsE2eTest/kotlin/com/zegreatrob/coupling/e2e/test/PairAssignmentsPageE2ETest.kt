package com.zegreatrob.coupling.e2e.test

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairCallSigns
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getRetiredPlayersButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getStatisticsButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getViewHistoryButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.KtorCouplingSdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import korlibs.time.DateTime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

@Suppress("unused")
class PairAssignmentsPageE2ETest {

    companion object {
        private suspend fun KtorCouplingSdk.save(party: PartyDetails, players: List<Player>) = coroutineScope {
            perform(SavePartyCommand(party))
            players.map { SavePlayerCommand(party.id, it) }
                .forEach { perform(it) }
        }
    }

    class GivenNoCurrentSetOfPairs {

        companion object {
            val party by lazy {
                PartyDetails(
                    PartyId("${randomInt()}-PairAssignmentsPageE2ETest"),
                    name = "${randomInt()}-PairAssignmentsPageE2ETest",
                )
            }

            val players by lazy {
                (1..5).map {
                    Player(
                        id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble",
                        avatarType = null,
                    )
                }
            }

            private val template = e2eSetup.extend(sharedSetup = { sdk ->
                sdk.save(party, players)
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) =
                template(context = PairAssignmentsPage, additionalActions = additionalSetup)
        }

        @Test
        fun showsThePartyAndRoster() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(party.id)
        } verify {
            PartyCard.element.text()
                .assertIsEqualTo(party.name)
            PlayerRoster.getPlayerElements("Unpaired players").map { it.text() }.toList()
                .assertIsEqualTo(players.map { it.name })
        }

        @Test
        fun willLetYouAddPlayers() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            PlayerRoster.getAddPlayerButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/player/new/"))
        }

        @Test
        fun willLetYouEditAnExistingPlayer() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            PlayerRoster.getPlayerElements("Unpaired players").first().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/player/${players[0].id}/"))
        }

        @Test
        fun willLetYouViewHistory() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getViewHistoryButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/history/"))
        }

        @Test
        fun willLetYouPrepareNewPairs() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getNewPairsButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/prepare/"))
        }

        @Test
        fun willLetYouGoToTheStatsPage() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getStatisticsButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/statistics"))
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getRetiredPlayersButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/players/retired"))
        }
    }

    class GivenCurrentSetOfPairsExists {

        companion object {
            val party by lazy {
                PartyDetails(
                    PartyId("${randomInt()}-PairAssignmentsPageE2ETest"),
                    name = "${randomInt()}-PairAssignmentsPageE2ETest",
                )
            }

            private val players by lazy {
                (1..5).map {
                    Player(
                        id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble",
                        avatarType = null,
                    )
                }
            }
            private val pairAssignmentDocument by lazy {
                PairAssignmentDocument(
                    id = PairAssignmentDocumentId("${uuid4()}"),
                    date = DateTime(year = 2015, month = 5, day = 30),
                    pairs = listOf(
                        pairOf(players[0], players[2]).withPins(emptySet()),
                        pairOf(players[4]).withPins(emptySet()),
                    ),
                )
            }

            private fun PinnedCouplingPair.players() = toPair().asArray().toList()

            private val unpairedPlayers = players - (pairAssignmentDocument.pairs.flatMap { it.players() }).toSet()

            private val setup = e2eSetup.extend(beforeAll = {
                sdk.await().apply {
                    perform(SavePartyCommand(party))
                    coroutineScope {
                        launch { players.forEach { perform(SavePlayerCommand(party.id, it)) } }
                        launch { perform(SavePairAssignmentsCommand(party.id, pairAssignmentDocument)) }
                    }
                }
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) =
                setup(context = PairAssignmentsPage, additionalActions = additionalSetup)
        }

        @Test
        fun willShowPlayersInCorrectPlaces() = currentPairAssignmentPageSetup {
        } exercise {
            goTo(party.id)
        } verify {
            assignedPairElements.assertTheMostRecentPairsAreShown()
            PlayerRoster.getPlayerElements("Unpaired players")
                .assertOnlyUnpairedPlayersAreShown()
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
        fun whenThePartyHasCallSignsTurnedOffTheyDoNotDisplay() = currentPairAssignmentPageSetup {
            sdk.await().apply {
                perform(SavePartyCommand(party.copy(callSignsEnabled = false)))
            }
        } exercise {
            goTo(party.id)
        } verify {
            assignedPairCallSigns.count()
                .assertIsEqualTo(0)
        }

        @Test
        fun whenThePartyHasCallSignsTurnedOnTheyDisplay() = currentPairAssignmentPageSetup {
            sdk.await().apply {
                perform(SavePartyCommand(party.copy(callSignsEnabled = true)))
            }
            WelcomePage.goTo()
        } exercise {
            goTo(party.id)
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

fun resolve(base: String, path: String) = if (base == "") {
    "/$path"
} else {
    "$base/$path"
}

val clientBasename get() = "${process.env.CLIENT_BASENAME}".let { if (it.isNotEmpty()) "/$it" else "" }