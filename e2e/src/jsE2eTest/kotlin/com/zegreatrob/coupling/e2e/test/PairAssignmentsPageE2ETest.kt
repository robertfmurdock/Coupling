package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairCallSigns
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getRetiredPlayersButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getStatisticsButton
import com.zegreatrob.coupling.e2e.test.ConfigHeader.getViewHistoryButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.notEmptyListOf
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class PairAssignmentsPageE2ETest {

    companion object {
        private suspend fun ActionCannon<CouplingSdkDispatcher>.save(party: PartyDetails, players: List<Player>) = coroutineScope {
            fire(SavePartyCommand(party))
            players.map { SavePlayerCommand(party.id, it) }
                .forEach { fire(it) }
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
                    defaultPlayer.copy(
                        id = PlayerId("${randomInt()}-PairAssignmentsPageE2ETest-$it".toNotBlankString().getOrThrow()),
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble",
                    )
                }
            }

            private val template = e2eSetup.extend(sharedSetup = { sdk ->
                sdk.save(party, players)
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) = template(context = PairAssignmentsPage, additionalActions = additionalSetup)
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
                .assertIsEqualTo(("/${party.id.value}/player/new/"))
        }

        @Test
        fun willLetYouEditAnExistingPlayer() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            PlayerRoster.getPlayerElements("Unpaired players").first().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/player/${players[0].id.value}/"))
        }

        @Test
        fun willLetYouViewHistory() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getViewHistoryButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/history/"))
        }

        @Test
        fun willLetYouPrepareNewPairs() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getNewPairsButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/prepare/"))
        }

        @Test
        fun willLetYouGoToTheStatsPage() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getStatisticsButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/statistics"))
        }

        @Test
        fun willLetYouGoToTheRetiredPlayersPage() = currentPairAssignmentPageSetup {
            goTo(party.id)
        } exercise {
            getRetiredPlayersButton().click()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/players/retired"))
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
                    defaultPlayer.copy(
                        id = PlayerId("${randomInt()}-PairAssignmentsPageE2ETest-$it".toNotBlankString().getOrThrow()),
                        name = "player$it",
                        callSignAdjective = "nimble",
                        callSignNoun = "thimble",
                    )
                }
            }
            private val pairingSet by lazy {
                PairingSet(
                    id = PairingSetId.new(),
                    date = LocalDateTime(2015, 5, 30, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()),
                    pairs = notEmptyListOf(
                        pairOf(players[0], players[2]).withPins(emptySet()),
                        pairOf(players[4]).withPins(emptySet()),
                    ),
                )
            }

            private fun PinnedCouplingPair.players() = toPair()

            private val unpairedPlayers = players - (pairingSet.pairs.toList().flatMap { it.players() })
                .toSet()

            private val setup = e2eSetup.extend(beforeAll = {
                sdk.await().apply {
                    fire(SavePartyCommand(party))
                    coroutineScope {
                        launch { players.forEach { fire(SavePlayerCommand(party.id, it)) } }
                        launch { sdk.await().fire(SavePairAssignmentsCommand(party.id, pairingSet)) }
                    }
                }
            })

            private fun currentPairAssignmentPageSetup(additionalSetup: suspend PairAssignmentsPage.() -> Unit) = setup(context = PairAssignmentsPage, additionalActions = additionalSetup)
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
                .assertIsEqualTo(pairingSet.pairs.toList()[0].players().map(Player::name))
            get(1).getPairPlayerNames()
                .assertIsEqualTo(pairingSet.pairs.toList()[1].players().map(Player::name))
        }

        private suspend fun WebdriverElementArray.assertOnlyUnpairedPlayersAreShown() {
            map(WebdriverElement::text).toList()
                .assertIsEqualTo(unpairedPlayers.map(Player::name))
        }

        @Test
        fun whenThePartyHasCallSignsTurnedOffTheyDoNotDisplay() = currentPairAssignmentPageSetup {
            sdk.await().apply {
                fire(SavePartyCommand(party.copy(callSignsEnabled = false)))
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
                fire(SavePartyCommand(party.copy(callSignsEnabled = true)))
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

        private suspend fun WebdriverElement.getPairPlayerNames() = all(PlayerCard.PLAYER_LOCATOR).map { it.text() }
    }
}
