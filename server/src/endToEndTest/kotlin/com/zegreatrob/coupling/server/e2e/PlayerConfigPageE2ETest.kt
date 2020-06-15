package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.test.Test

@Suppress("unused")
class PlayerConfigPageE2ETest {
    class WithOneTribeOnePlayer : PlayerConfigOnePlayerTest(Companion::buildTribe, Companion::buildPlayer) {
        companion object {
            fun buildTribe() = Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E"))
            fun buildPlayer() =
                Player("${randomInt()}-PlayerConfigPageE2E", name = "${randomInt()}-PlayerConfigPageE2E")
        }

        @Test
        fun whenNothingHasChangedWillNotAlertOnLeaving() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
            page.goTo(tribe.id, player.id)
        } exercise {
            TribeCard.element.performClick()
        } verify {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
        }

        @Test
        fun whenNameIsChangedWillGetAlertOnLeaving() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
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
        fun whenNameIsChangedThenSaveWillNotGetAlertOnLeaving() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
            val newName = "completely different name"
        }) {
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
        fun savingWithNoNameWillShowDefaultName() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
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
        fun whenRetireIsClickedWillAlertAndOnAcceptRedirect() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
            page.goTo(tribe.id, player.id)
        } exercise {
            page.deleteButton.performClick()
            browser.switchTo().alert().await()
                .accept()
        } verify {
            page.waitToArriveAt("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
        }

        @Test
        fun whenTribeDoesNotHaveBadgingEnabledWillNotShowBadgeSelector() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
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
        fun willShowAllPlayers() = testPlayerConfig(object : PlayersContext() {
            val page = PlayerConfig
        }) {
            page.goTo(tribe.id, players[0].id)
        } exercise {
            PlayerRoster.playerElements.map { element -> element.getText() }.await().toList()
        } verify { result ->
            result.assertIsEqualTo(players.map { it.name })
        }

        companion object {

            val template = asyncTestTemplate(
                sharedSetup = {},
                sharedTeardown = { checkLogs() }
            )

            fun <C : PlayersContext> testPlayerConfig(
                context: C,
                additionalActions: suspend C.() -> Unit = {}
            ) = template(
                contextProvider = {
                    context.attachPlayers(
                        playersProvider.await(),
                        tribeProvider.await(),
                        sdkProvider.await()
                    )
                },
                additionalActions = additionalActions
            )

            private val tribeProvider by lazy {
                GlobalScope.async {
                    val sdk = sdkProvider.await()
                    Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E"))
                        .also { sdk.save(it) }
                }
            }

            private val playersProvider by lazy {
                GlobalScope.async {
                    val sdk = sdkProvider.await()
                    val tribe = tribeProvider.await()
                    val players = generateSequence {
                        Player(
                            "${randomInt()}-PlayerConfigPageE2E",
                            name = "${randomInt()}-PlayerConfigPageE2E"
                        )
                    }.take(5).toList()
                    players.also {
                        it.forEach { player -> sdk.save(tribe.id.with(player)) }
                    }
                }
            }
        }

    }

    class WhenTribeHasBadgingEnabled : PlayerConfigOnePlayerTest(::buildTribe, ::buildPlayer) {

        @Test
        fun willShowBadgeSelector() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) exercise {
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
        fun willSelectTheDefaultBadge() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) exercise {
            page.goTo(tribe.id, player.id)
        } verify {
            page.defaultBadgeOption.getAttribute("checked").await()
                .assertIsEqualTo("true")
        }

        @Test
        fun willRememberBadgeSelection() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
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

        companion object {
            fun buildTribe() = Tribe(
                TribeId("${randomInt()}-PlayerConfigPageE2E"),
                badgesEnabled = true,
                defaultBadgeName = "Badge 1",
                alternateBadgeName = "Badge 2"
            )

            fun buildPlayer() =
                Player("${randomInt()}-PlayerConfigPageE2E", name = "${randomInt()}-PlayerConfigPageE2E")

        }

    }

    class WhenTribeHasCallSignsEnabled : PlayerConfigOnePlayerTest(Companion::buildTribe, Companion::buildPlayer) {
        companion object {
            fun buildTribe() = Tribe(
                TribeId("${randomInt()}-PlayerConfigPageE2E"),
                callSignsEnabled = true
            )

            fun buildPlayer() = Player(
                "${randomInt()}-PlayerConfigPageE2E",
                name = "${randomInt()}-PlayerConfigPageE2E"
            )
        }

        @Test
        fun adjectiveAndNounCanBeSaved() = testPlayerConfig(object : PlayerContext() {
            val page = PlayerConfig
        }) {
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
        val template = asyncTestTemplate(
            sharedSetup = {},
            sharedTeardown = { checkLogs() }
        )

        private fun <C : TribeContext> testPlayerConfig(
            context: C,
            additionalActions: suspend C.() -> Unit = {}
        ) = template(
            contextProvider = { context.attachTribe(tribeProvider.await(), sdkProvider.await()) },
            additionalActions = additionalActions
        )

        private val tribeProvider by lazy {
            GlobalScope.async {
                val sdk = sdkProvider.await()
                buildTribe().also { sdk.save(it) }
            }
        }

        private fun buildTribe() = Tribe(
            TribeId("${randomInt()}-WithOneTribeNoPlayers"),
            callSignsEnabled = true
        )

        @Test
        fun willSuggestCallSign() = testPlayerConfig(object : TribeContext() {
            val page = PlayerConfig
        }) exercise {
            page.goToNew(tribe.id)
        } verify {
            page.adjectiveTextInput.getAttribute("value").await()
                .assertIsNotEqualTo("")
            page.nounTextInput.getAttribute("value").await()
                .assertIsNotEqualTo("")
        }
    }
}

abstract class PlayerConfigOnePlayerTest(val buildTribe: () -> Tribe, val buildPlayer: () -> Player) {

    val templateSetup = asyncTestTemplate(
        sharedSetup = {},
        sharedTeardown = { checkLogs() }
    )

    fun <C : PlayerContext> testPlayerConfig(
        context: C,
        additionalActions: suspend C.() -> Unit = {}
    ) = templateSetup(
        contextProvider = { context.attachPlayer(playerProvider.await(), tribeProvider.await(), sdkProvider.await()) },
        additionalActions = additionalActions
    )

    private val tribeProvider by lazy {
        GlobalScope.async {
            val sdk = sdkProvider.await()
            buildTribe().also { sdk.save(it) }
        }
    }

    private val playerProvider by lazy {
        GlobalScope.async {
            val sdk = sdkProvider.await()
            val tribe = tribeProvider.await()
            buildPlayer().also { sdk.save(tribe.id.with(it)) }
        }
    }

}