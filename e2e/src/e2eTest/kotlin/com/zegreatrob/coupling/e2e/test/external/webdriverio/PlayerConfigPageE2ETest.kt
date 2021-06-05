package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.ConfigForm.deleteButton
import com.zegreatrob.coupling.e2e.test.external.webdriverio.ConfigForm.saveButton
import com.zegreatrob.coupling.e2e.test.external.webdriverio.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.external.webdriverio.PlayerCard.header
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import kotlin.test.Test

@Suppress("unused")
class PlayerConfigPageE2ETest {

    companion object {
        private fun playerConfigOnePlayerSetup(buildTribe: () -> Tribe, buildPlayer: () -> Player) =
            e2eSetup.extend(beforeAll = {
                val sdk = sdkProvider.await()
                val tribe = buildTribe()
                sdk.save(tribe)

                val player = buildPlayer()
                sdk.save(tribe.id.with(player))

                Triple(player, tribe, sdkProvider.await())
            })
    }

    class WithOneTribeOnePlayer {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildTribe = { Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E")) },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E"
                    )
                }
            )
        }

        @Test
        fun whenNothingHasChangedWillNotAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } exercise {
            TribeCard.element().click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/pairAssignments/current/"))
        }

        @Test
        fun whenNameIsChangedWillGetAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
            PlayerConfigPage.playerNameTextField().setValue("completely different name")
        } exercise {
            TribeCard.element().click()
            WebdriverBrowser.waitForAlert()
            WebdriverBrowser.alertText().also {
                WebdriverBrowser.acceptAlert()
                PairAssignmentsPage.waitForPage()
            }
        } verify { alertText ->
            alertText.assertIsEqualTo("You have unsaved data. Press OK to leave without saving.")
        }

        @Test
        fun whenNameIsChangedThenSaveWillNotGetAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
            val newName = "completely different name"
        }.attachPlayer()) {
            with(page) {
                goTo(tribe.id, player.id)
                playerNameTextField().setValue(newName)
                saveButton.click()
                waitForSaveToComplete(newName)
            }
        } exercise {
            TribeCard.element().click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/pairAssignments/current/"))
            PlayerConfigPage.goTo(tribe.id, player.id)
            PlayerConfigPage.playerNameTextField().attribute("value")
                .assertIsEqualTo(newName)
        }

        @Test
        fun savingWithNoNameWillShowDefaultName() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
            PlayerConfigPage.playerNameTextField().clearSetValue(" ")
            PlayerConfigPage.playerNameTextField().clearSetValue("")
            saveButton.click()
            PlayerConfigPage.waitForSaveToComplete("Unknown")
            PlayerConfigPage.waitForPage()
        } exercise {
            TribeCard.element().click()
            PairAssignmentsPage.waitForPage()
        } verify {
            WebdriverBrowser.currentUrl().pathname
                .assertIsEqualTo(resolve(clientBasename, "${tribe.id.value}/pairAssignments/current/"))
            PlayerConfigPage.goTo(tribe.id, player.id)
            header.text()
                .assertIsEqualTo("Unknown")
        }

        @Test
        fun whenRetireIsClickedWillAlertAndOnAcceptRedirect() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } exercise {
            deleteButton.click()
            WebdriverBrowser.acceptAlert()
        } verify {
            page.waitToArriveAt(resolve(clientBasename, "${tribe.id.value}/pairAssignments/current/"))
        }

        @Test
        fun whenTribeDoesNotHaveBadgingEnabledWillNotShowBadgeSelector() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            sdk.save(tribe.copy(badgesEnabled = false))
        } exercise {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } verify {
            PlayerConfigPage.defaultBadgeOption().isPresent()
                .assertIsEqualTo(false)
            PlayerConfigPage.altBadgeOption().isPresent()
                .assertIsEqualTo(false)
        }
    }

    class WithTribeWithManyPlayers {

        @Test
        fun willShowAllPlayers() = e2eSetup(object {
            val tribe = Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E"))
            val players = generateSequence {
                Player(
                    id = "${randomInt()}-PlayerConfigPageE2E",
                    name = "${randomInt()}-PlayerConfigPageE2E"
                )
            }.take(5).toList()
            val page = PlayerConfigPage
        }) {
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            players.forEach { player -> sdk.save(tribe.id.with(player)) }
            PlayerConfigPage.goTo(tribe.id, players[0].id)
        } exercise {
            PlayerRoster.playerElements.map { element -> element.text() }.toList()
        } verify { result ->
            result.assertIsEqualTo(players.map { it.name })
        }

    }

    class WhenTribeHasBadgingEnabled {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildTribe = {
                    Tribe(
                        TribeId("${randomInt()}-PlayerConfigPageE2E"),
                        badgesEnabled = true,
                        defaultBadgeName = "Badge 1",
                        alternateBadgeName = "Badge 2"
                    )
                },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E"
                    )
                }
            )
        }

        @Test
        fun willShowBadgeSelector() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) exercise {
            PlayerConfigPage.goTo(tribe.id, player.id)
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
        fun willSelectTheDefaultBadge() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) exercise {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } verify {
            PlayerConfigPage.defaultBadgeOption().attribute("checked")
                .assertIsEqualTo("true")
        }

        @Test
        fun willRememberBadgeSelection() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } exercise {
            PlayerConfigPage.altBadgeOption().click()
            saveButton.click()
            PlayerConfigPage.waitForSaveToComplete(player.name)
        } verify {
            PlayerConfigPage.goTo(tribe.id, player.id)
            PlayerConfigPage.altBadgeOption().attribute("checked")
                .assertIsEqualTo("true")
        }

    }

    class WhenTribeHasCallSignsEnabled {

        companion object {
            private val playerSetup = playerConfigOnePlayerSetup(
                buildTribe = {
                    Tribe(
                        TribeId("${randomInt()}-PlayerConfigPageE2E"),
                        callSignsEnabled = true
                    )
                },
                buildPlayer = {
                    Player(
                        "${randomInt()}-PlayerConfigPageE2E",
                        name = "${randomInt()}-PlayerConfigPageE2E"
                    )
                }
            )
        }

        @Test
        fun adjectiveAndNounCanBeSaved() = playerSetup(object : PlayerContext() {
            val page = PlayerConfigPage
        }.attachPlayer()) {
            PlayerConfigPage.goTo(tribe.id, player.id)
        } exercise {
            PlayerConfigPage.adjectiveTextInput().clearSetValue("Superior")
            PlayerConfigPage.nounTextInput().clearSetValue("Spider-Man")
            saveButton.click()
            PlayerConfigPage.waitForSaveToComplete(player.name)
        } verify {
            PlayerConfigPage.goTo(tribe.id, player.id)
            PlayerConfigPage.adjectiveTextInput().attribute("value")
                .assertIsEqualTo("Superior")
            PlayerConfigPage.nounTextInput().attribute("value")
                .assertIsEqualTo("Spider-Man")
        }

    }

    class WithOneTribeNoPlayers {

        @Test
        fun willSuggestCallSign() = e2eSetup(object {
            val tribe = Tribe(
                id = TribeId("${randomInt()}-WithOneTribeNoPlayers"),
                callSignsEnabled = true
            )
        }) {
            val sdk = sdkProvider.await()
            sdk.save(tribe)
        } exercise {
            PlayerConfigPage.goToNew(tribe.id)
        } verify {
            PlayerConfigPage.adjectiveTextInput().attribute("value")
                .assertIsNotEqualTo("")
            PlayerConfigPage.nounTextInput().attribute("value")
                .assertIsNotEqualTo("")
        }
    }
}
