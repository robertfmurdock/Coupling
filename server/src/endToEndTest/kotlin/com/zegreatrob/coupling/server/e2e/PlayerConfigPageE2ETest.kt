package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
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

        private val playerSetup = playerConfigOnePlayerSetup(
            buildTribe = { Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E")) },
            buildPlayer = { Player("${randomInt()}-PlayerConfigPageE2E", name = "${randomInt()}-PlayerConfigPageE2E") }
        )

        @Test
        fun whenNothingHasChangedWillNotAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
        } exercise {
            TribeCard.element.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
        }

        @Test
        fun whenNameIsChangedWillGetAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
            with(page.playerNameTextField) {
                performClear()
                performSendKeys("completely different name")
            }
        } exercise {
            TribeCard.element.performClick()
            browser.switchTo().alert().await()
        } verify { alert ->
            val text = alert.getText().await()
                .also { alert.dismiss().await() }
            text.assertIsEqualTo("You have unsaved data. Would you like to save before you leave?")
        }

        @Test
        fun whenNameIsChangedThenSaveWillNotGetAlertOnLeaving() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
            val newName = "completely different name"
        }.attachPlayer()) {
            with(page) {
                goTo(tribe.id, player.id)
                with(playerNameTextField) {
                    performClear()
                    performSendKeys(newName)
                }
                saveButton.performClick()
                waitForSaveToComplete(newName)
            }
        } exercise {
            TribeCard.element.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
            page.goTo(tribe.id, player.id)
            page.playerNameTextField.getAttribute("value").await()
                .assertIsEqualTo(newName)
        }

        @Test
        fun savingWithNoNameWillShowDefaultName() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
            with(page.playerNameTextField) {
                performClear()
                performSendKeys(" \b")
            }
            page.saveButton.performClick()
            page.waitForSaveToComplete("Unknown")
            page.waitForPage()
        } exercise {
            TribeCard.element.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
            page.goTo(tribe.id, player.id)
            PlayerCard.header.getText().await()
                .assertIsEqualTo("Unknown")
        }

        @Test
        fun whenRetireIsClickedWillAlertAndOnAcceptRedirect() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
        } exercise {
            page.deleteButton.performClick()
            browser.switchTo().alert().await()
                .accept()
        } verify {
            page.waitToArriveAt("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
        }

        @Test
        fun whenTribeDoesNotHaveBadgingEnabledWillNotShowBadgeSelector() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            sdk.save(tribe.copy(badgesEnabled = false))
        } exercise {
            page.goTo(tribe.id, player.id)
        } verify {
            page.defaultBadgeOption.isPresent().await()
                .assertIsEqualTo(false)
            page.altBadgeOption.isPresent().await()
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
            val page = PlayerConfig
        }) {
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            players.forEach { player -> sdk.save(tribe.id.with(player)) }
            page.goTo(tribe.id, players[0].id)
        } exercise {
            PlayerRoster.playerElements.map { element -> element.getText() }.await().toList()
        } verify { result ->
            result.assertIsEqualTo(players.map { it.name })
        }

    }

    class WhenTribeHasBadgingEnabled {

        private val playerSetup = playerConfigOnePlayerSetup(
            buildTribe = {
                Tribe(
                    TribeId("${randomInt()}-PlayerConfigPageE2E"),
                    badgesEnabled = true,
                    defaultBadgeName = "Badge 1",
                    alternateBadgeName = "Badge 2"
                )
            },
            buildPlayer = { Player("${randomInt()}-PlayerConfigPageE2E", name = "${randomInt()}-PlayerConfigPageE2E") }
        )

        @Test
        fun willShowBadgeSelector() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) exercise {
            page.goTo(tribe.id, player.id)
        } verify {
            page.defaultBadgeOption.isDisplayed().await()
                .assertIsEqualTo(true)
            element(By.css("option[value=\"1\"]"))
                .getAttribute("label")
                .await()
                .assertIsEqualTo("Badge 1")
            page.altBadgeOption.isDisplayed().await()
                .assertIsEqualTo(true)
            element(By.css("option[value=\"2\"]"))
                .getAttribute("label")
                .await()
                .assertIsEqualTo("Badge 2")
        }

        @Test
        fun willSelectTheDefaultBadge() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) exercise {
            page.goTo(tribe.id, player.id)
        } verify {
            page.defaultBadgeOption.getAttribute("checked").await()
                .assertIsEqualTo("true")
        }

        @Test
        fun willRememberBadgeSelection() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
        } exercise {
            page.altBadgeOption.performClick()
            page.saveButton.performClick()
            page.waitForSaveToComplete(player.name)
        } verify {
            page.goTo(tribe.id, player.id)
            page.altBadgeOption.getAttribute("checked").await()
                .assertIsEqualTo("true")
        }

    }

    class WhenTribeHasCallSignsEnabled {

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

        @Test
        fun adjectiveAndNounCanBeSaved() = playerSetup(object : PlayerContext() {
            val page = PlayerConfig
        }.attachPlayer()) {
            page.goTo(tribe.id, player.id)
        } exercise {
            with(page.adjectiveTextInput) {
                performClear()
                performSendKeys("Superior")
            }
            with(page.nounTextInput) {
                performClear()
                performSendKeys("Spider-Man")
            }
            page.saveButton.performClick()
            page.waitForSaveToComplete(player.name)
        } verify {
            page.goTo(tribe.id, player.id)
            page.adjectiveTextInput.getAttribute("value").await()
                .assertIsEqualTo("Superior")
            page.nounTextInput.getAttribute("value").await()
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
            PlayerConfig.goToNew(tribe.id)
        } verify {
            PlayerConfig.adjectiveTextInput.getAttribute("value").await()
                .assertIsNotEqualTo("")
            PlayerConfig.nounTextInput.getAttribute("value").await()
                .assertIsNotEqualTo("")
        }
    }
}
