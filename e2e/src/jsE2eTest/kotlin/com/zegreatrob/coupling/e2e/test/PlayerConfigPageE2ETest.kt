package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.ConfigForm.retireButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.saveButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.e2e.test.PlayerCard.playerElements
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.uuid.Uuid

@Suppress("unused")
class PlayerConfigPageE2ETest {

    companion object {
        private fun playerConfigOnePlayerSetup(buildParty: () -> PartyDetails, buildPlayer: () -> Player) = e2eSetup.extend(beforeAll = {
            val party = buildParty()
            val player = buildPlayer()
            sdk.await().apply {
                fire(SavePartyCommand(party))
                fire(SavePlayerCommand(party.id, player))
            }
            Triple(player, party, sdk.await())
        })
    }

    @Test
    fun newPlayerWillAlwaysBeUnique() = e2eSetup(object {
        val party = stubPartyDetails()
        val page = PlayerConfigPage
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        page.goToNew(party.id)
        page.playerNameTextField().setValue("1")
        saveButton().click()
        page.waitForSaveToComplete("1")
        page.playerNameTextField().setValue("2")
        saveButton().click()
        page.waitForSaveToComplete("2")
        page.playerNameTextField().setValue("3")
        saveButton().click()
        page.waitForSaveToComplete("3")
    } verify {
        sdk().fire(graphQuery { party(party.id) { playerList() } })
            ?.party?.playerList?.elements?.map { it.name }
            .assertIsEqualTo(listOf("1", "2", "3"))
    }

    class WithOnePartyOnePlayer {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildParty = {
                    PartyDetails(
                        PartyId("${randomInt()}-PlayerConfigPageE2E"),
                        name = "${Uuid.random()}",
                    )
                },
                buildPlayer = {
                    defaultPlayer.copy(
                        id = PlayerId("${randomInt()}-PlayerConfigPageE2E".toNotBlankString().getOrThrow()),
                        name = "${randomInt()}-PlayerConfigPageE2E",
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
                .assertIsEqualTo(("/${party.id.value}/pairAssignments/current/"))
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
                saveButton().click()
                waitForSaveToComplete(newName)
            }
        } exercise {
            element.click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/pairAssignments/current/"))
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
            saveButton().click()
            PlayerConfigPage.waitForSaveToComplete("Unknown")
            PlayerConfigPage.waitForPage()
        } exercise {
            element.click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(("/${party.id.value}/pairAssignments/current/"))
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
            retireButton().click()
            WebdriverBrowser.acceptAlert()
        } verify {
            page.waitToArriveAt(("/${party.id.value}/pairAssignments/current/"))
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
                defaultPlayer.copy(
                    id = PlayerId("${randomInt()}-PlayerConfigPageE2E".toNotBlankString().getOrThrow()),
                    name = "${randomInt()}-PlayerConfigPageE2E",
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
                    defaultPlayer.copy(
                        id = PlayerId("${randomInt()}-PlayerConfigPageE2E".toNotBlankString().getOrThrow()),
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
            WebdriverElement("option[value=\"Default\"]")
                .attribute("label")
                .assertIsEqualTo("Badge 1")
            PlayerConfigPage.altBadgeOption().isDisplayed()
                .assertIsEqualTo(true)
            WebdriverElement("option[value=\"Alternate\"]")
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
            saveButton().click()
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
                    defaultPlayer.copy(
                        id = PlayerId("${randomInt()}-PlayerConfigPageE2E".toNotBlankString().getOrThrow()),
                        name = "${randomInt()}-PlayerConfigPageE2E",
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
            saveButton().click()
            PlayerConfigPage.waitForSaveToComplete(player.name)
        } verify {
            PlayerConfigPage.goTo(party.id, player.id)
            PlayerConfigPage.adjectiveTextInput().attribute("value")
                .assertIsEqualTo("Superior")
            PlayerConfigPage.nounTextInput().attribute("value")
                .assertIsEqualTo("Spider-Man")
        }
    }

    class WhenPlayerIsDeleted {
        @Test
        fun playerAttributesCanBeSeen() = e2eSetup(object {
            val party = stubPartyDetails()
            val player = stubPlayer()
        }) {
            sdk().fire(SavePartyCommand(party))
            sdk().fire(SavePlayerCommand(party.id, player))
            sdk().fire(DeletePlayerCommand(party.id, player.id))
        } exercise {
            PlayerConfigPage.goTo(party.id, player.id)
        } verify { result ->
            PlayerConfigPage.playerNameTextField().attribute("value")
                .assertIsEqualTo(player.name)
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
