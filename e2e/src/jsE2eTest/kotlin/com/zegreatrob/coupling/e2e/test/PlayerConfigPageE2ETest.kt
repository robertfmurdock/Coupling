package com.zegreatrob.coupling.e2e.test

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.ConfigForm.getDeleteButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.getSaveButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.e2e.test.PlayerCard.playerElements
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import kotlin.test.Test

@Suppress("unused")
class PlayerConfigPageE2ETest {

    companion object {
        private fun playerConfigOnePlayerSetup(buildParty: () -> PartyDetails, buildPlayer: () -> Player) =
            e2eSetup.extend(beforeAll = {
                val party = buildParty()
                val player = buildPlayer()
                sdk.await().apply {
                    fire(SavePartyCommand(party))
                    fire(SavePlayerCommand(party.id, player))
                }
                Triple(player, party, sdk.await())
            })
    }

    class WithOnePartyOnePlayer {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildParty = {
                    PartyDetails(
                        PartyId("${randomInt()}-PlayerConfigPageE2E"),
                        name = "${uuid4()}",
                    )
                },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E",
                        avatarType = null,
                    )
                },
            )
        }

        @Test
        fun whenNothingHasChangedWillNotAlertOnLeaving() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
        } exercise {
            element.click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/pairAssignments/current/"))
        }

        @Test
        fun whenNameIsChangedWillGetAlertOnLeaving() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.playerNameTextField().setValue("completely different name")
        } exercise {
            element.click()
            WebdriverBrowser.waitForAlert()
            WebdriverBrowser.alertText().also {
                WebdriverBrowser.acceptAlert()
                PairAssignmentsPage.waitForPage()
            }
        } verify { alertText ->
            alertText.assertIsEqualTo("You have unsaved data. Press OK to leave without saving.")
        }

        @Test
        fun whenNameIsChangedThenSaveWillNotGetAlertOnLeaving() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
                val newName = "completely different name"
            }.attachPlayer(),
        ) {
            with(page) {
                goTo(party.id, player.id)
                playerNameTextField().setValue(newName)
                getSaveButton().click()
                waitForSaveToComplete(newName)
            }
        } exercise {
            element.click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/pairAssignments/current/"))
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.playerNameTextField().attribute("value")
                .assertIsEqualTo(newName)
        }

        @Test
        fun savingWithNoNameWillShowDefaultName() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.playerNameTextField().clearSetValue(" ")
            PlayerConfigPage.playerNameTextField().clearSetValue("")
            getSaveButton().click()
            PlayerConfigPage.waitForSaveToComplete("Unknown")
            PlayerConfigPage.waitForPage()
        } exercise {
            element.click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${party.id.value}/pairAssignments/current/"))
            PlayerConfigPage.goTo(party.id, player.id)
            playerElements.first()
                .text()
                .assertIsEqualTo("Unknown")
        }

        @Test
        fun whenRetireIsClickedWillAlertAndOnAcceptRedirect() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
        } exercise {
            getDeleteButton().click()
            WebdriverBrowser.acceptAlert()
        } verify {
            page.waitToArriveAt(resolve(clientBasename, "${party.id.value}/pairAssignments/current/"))
        }

        @Test
        fun whenPartyDoesNotHaveBadgingEnabledWillNotShowBadgeSelector() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            sdk.fire(SavePartyCommand(party.copy(badgesEnabled = false)))
        } exercise {
            PlayerConfigPage.goTo(party.id, player.id)
        } verify {
            PlayerConfigPage.defaultBadgeOption().isPresent()
                .assertIsEqualTo(false)
            PlayerConfigPage.altBadgeOption().isPresent()
                .assertIsEqualTo(false)
        }
    }

    class WithPartyWithManyPlayers {

        @Test
        fun willShowAllPlayers() = e2eSetup(object {
            val party = PartyDetails(PartyId("${randomInt()}-PlayerConfigPageE2E"))
            val players = generateSequence {
                Player(
                    id = "${randomInt()}-PlayerConfigPageE2E",
                    name = "${randomInt()}-PlayerConfigPageE2E",
                    avatarType = null,
                )
            }.take(5).toList()
            val page = PlayerConfigPage
        }) {
            sdk.await().apply {
                fire(SavePartyCommand(party))
                players.forEach { player -> fire(SavePlayerCommand(party.id, player)) }
            }
            PlayerConfigPage.goTo(party.id, players[0].id)
        } exercise {
            PlayerRoster.getPlayerElements("Players").map { element -> element.text() }.toList()
        } verify { result ->
            result.assertIsEqualTo(players.map { it.name })
        }
    }

    class WhenPartyHasBadgingEnabled {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildParty = {
                    PartyDetails(
                        PartyId("${randomInt()}-PlayerConfigPageE2E"),
                        badgesEnabled = true,
                        defaultBadgeName = "Badge 1",
                        alternateBadgeName = "Badge 2",
                    )
                },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E",
                        avatarType = null,
                    )
                },
            )
        }

        @Test
        fun willShowBadgeSelector() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) exercise {
            PlayerConfigPage.goTo(party.id, player.id)
        } verify {
            PlayerConfigPage.defaultBadgeOption().isDisplayed()
                .assertIsEqualTo(true)
            WebdriverElement("option[value=\"1\"]")
                .attribute("label")
                .assertIsEqualTo("Badge 1")
            PlayerConfigPage.altBadgeOption().isDisplayed()
                .assertIsEqualTo(true)
            WebdriverElement("option[value=\"2\"]")
                .attribute("label")
                .assertIsEqualTo("Badge 2")
        }

        @Test
        fun willSelectTheDefaultBadge() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) exercise {
            PlayerConfigPage.goTo(party.id, player.id)
        } verify {
            PlayerConfigPage.defaultBadgeOption().isSelected()
                .assertIsEqualTo(true)
        }

        @Test
        fun willRememberBadgeSelection() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
        } exercise {
            PlayerConfigPage.altBadgeOption().click()
            getSaveButton().click()
            PlayerConfigPage.waitForSaveToComplete(player.name)
        } verify {
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.altBadgeOption().isSelected()
                .assertIsEqualTo(true)
        }
    }

    class WhenPartyHasCallSignsEnabled {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildParty = {
                    PartyDetails(
                        PartyId("${randomInt()}-PlayerConfigPageE2E"),
                        callSignsEnabled = true,
                    )
                },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E",
                        avatarType = null,
                    )
                },
            )
        }

        @Test
        fun adjectiveAndNounCanBeSaved() = playerSetup.with(
            object : PlayerContext() {
                val page = PlayerConfigPage
            }.attachPlayer(),
        ) {
            PlayerConfigPage.goTo(party.id, player.id)
        } exercise {
            PlayerConfigPage.adjectiveTextInput().clearSetValue("Superior")
            PlayerConfigPage.nounTextInput().clearSetValue("Spider-Man")
            getSaveButton().click()
            PlayerConfigPage.waitForSaveToComplete(player.name)
        } verify {
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.adjectiveTextInput().attribute("value")
                .assertIsEqualTo("Superior")
            PlayerConfigPage.nounTextInput().attribute("value")
                .assertIsEqualTo("Spider-Man")
        }
    }

    class WithOnePartyNoPlayers {

        @Test
        fun willSuggestCallSign() = e2eSetup(object {
            val party = PartyDetails(
                id = PartyId("${randomInt()}-WithOnePartyNoPlayers"),
                callSignsEnabled = true,
            )
        }) {
            sdk.await().apply {
                fire(SavePartyCommand(party))
            }
        } exercise {
            PlayerConfigPage.goToNew(party.id)
        } verify {
            PlayerConfigPage.adjectiveTextInput().attribute("value")
                .assertIsNotEqualTo("")
            PlayerConfigPage.nounTextInput().attribute("value")
                .assertIsNotEqualTo("")
        }
    }
}
