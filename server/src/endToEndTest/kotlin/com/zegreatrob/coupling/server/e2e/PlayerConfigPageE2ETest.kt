package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
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
        fun whenNothingHasChangedWillNotAlertOnLeaving() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
            } exerciseAsync {
                TribeCard.element.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
            }
        }

        @Test
        fun whenNameIsChangedWillGetAlertOnLeaving() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
                with(playerNameTextField) {
                    performClear()
                    performSendKeys("completely different name")
                }
            } exerciseAsync {
                TribeCard.element.performClick()
                browser.switchTo().alert().await()
            } verifyAsync { alert ->
                val text = alert.getText().await()
                    .also { alert.dismiss().await() }
                text.assertIsEqualTo("You have unsaved data. Would you like to save before you leave?")
            }
        }

        @Test
        fun whenNameIsChangedThenSaveWillNotGetAlertOnLeaving() = testPlayerConfig { tribe, player ->
            setupAsync(object {
                val newName = "completely different name"
            }) {
                with(PlayerConfig) {
                    goTo(tribe.id, player.id)
                    with(playerNameTextField) {
                        performClear()
                        performSendKeys(newName)
                    }
                    saveButton.performClick()
                    waitForSaveToComplete(newName)
                }
            } exerciseAsync {
                TribeCard.element.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
                PlayerConfig.goTo(tribe.id, player.id)
                PlayerConfig.playerNameTextField.getAttribute("value").await()
                    .assertIsEqualTo(newName)
            }
        }

        @Test
        fun savingWithNoNameWillShowDefaultName() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
                with(playerNameTextField) {
                    performClear()
                    performSendKeys(" \b")
                }
                saveButton.performClick()
                waitForSaveToComplete("Unknown")
                waitForPage()
            } exerciseAsync {
                TribeCard.element.performClick()
            } verifyAsync {
                browser.getCurrentUrl().await()
                    .assertIsEqualTo("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
                goTo(tribe.id, player.id)
                PlayerCard.header.getText().await()
                    .assertIsEqualTo("Unknown")
            }
        }

        @Test
        fun whenRetireIsClickedWillAlertAndOnAcceptRedirect() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
            } exerciseAsync {
                deleteButton.performClick()
                browser.switchTo().alert().await()
                    .accept()
            } verifyAsync {
                waitToArriveAt("${browser.baseUrl}/${tribe.id.value}/pairAssignments/current/")
            }
        }

        @Test
        fun whenTribeDoesNotHaveBadgingEnabledWillNotShowBadgeSelector() = testPlayerConfig { tribe, player ->
            val sdk = sdkProvider.await()
            sdk.save(tribe.copy(badgesEnabled = false))
            setupAsync(PlayerConfig) {
            } exerciseAsync {
                goTo(tribe.id, player.id)
            } verifyAsync {
                defaultBadgeOption.isPresent().await()
                    .assertIsEqualTo(false)
                altBadgeOption.isPresent().await()
                    .assertIsEqualTo(false)
            }
        }
    }

    class WithTribeWithManyPlayers {

        @Test
        fun willShowAllPlayers() = testPlayerConfig { tribe, players ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, players[0].id)
            } exerciseAsync {
                PlayerRoster.playerElements.map { element -> element.getText() }.await().toList()
            } verifyAsync { result ->
                result.assertIsEqualTo(players.map { it.name })
            }
        }

        companion object {
            fun testPlayerConfig(handler: suspend CoroutineScope.(Tribe, List<Player>) -> Unit) = testAsync {
                val tribe = tribeProvider.await()
                val players = playersProvider.await()

                handler(tribe, players)
            }

            val tribeProvider by lazy {
                GlobalScope.async {
                    val sdk = sdkProvider.await()
                    Tribe(TribeId("${randomInt()}-PlayerConfigPageE2E"))
                        .also { sdk.save(it) }
                }
            }

            val playersProvider by lazy {
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
        fun willShowBadgeSelector() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) exerciseAsync {
                goTo(tribe.id, player.id)
            } verifyAsync {
                defaultBadgeOption.isDisplayed().await()
                    .assertIsEqualTo(true)
                element(By.css("option[value=\"1\"]"))
                    .getAttribute("label")
                    .await()
                    .assertIsEqualTo("Badge 1")
                altBadgeOption.isDisplayed().await()
                    .assertIsEqualTo(true)
                element(By.css("option[value=\"2\"]"))
                    .getAttribute("label")
                    .await()
                    .assertIsEqualTo("Badge 2")
            }
        }

        @Test
        fun willSelectTheDefaultBadge() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) exerciseAsync {
                goTo(tribe.id, player.id)
            } verifyAsync {
                defaultBadgeOption.getAttribute("checked").await()
                    .assertIsEqualTo("true")
            }
        }

        @Test
        fun willRememberBadgeSelection() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
            } exerciseAsync {
                altBadgeOption.performClick()
                saveButton.performClick()
                waitForSaveToComplete(player.name)
            } verifyAsync {
                goTo(tribe.id, player.id)
                altBadgeOption.getAttribute("checked").await()
                    .assertIsEqualTo("true")
            }
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
        fun adjectiveAndNounCanBeSaved() = testPlayerConfig { tribe, player ->
            setupAsync(PlayerConfig) {
                goTo(tribe.id, player.id)
            } exerciseAsync {
                with(adjectiveTextInput) {
                    performClear()
                    performSendKeys("Superior")
                }
                with(nounTextInput) {
                    performClear()
                    performSendKeys("Spider-Man")
                }
                saveButton.performClick()
                waitForSaveToComplete(player.name)
            } verifyAsync {
                goTo(tribe.id, player.id)
                adjectiveTextInput.getAttribute("value").await()
                    .assertIsEqualTo("Superior")
                nounTextInput.getAttribute("value").await()
                    .assertIsEqualTo("Spider-Man")
            }
        }

    }

    class WithOneTribeNoPlayers {
        private fun testPlayerConfig(handler: suspend CoroutineScope.(Tribe) -> Unit) = testAsync {
            val tribe = tribeProvider.await()
            handler(tribe)
        }

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
        fun willSuggestCallSign() = testPlayerConfig { tribe ->
            setupAsync(PlayerConfig) exerciseAsync {
                goToNew(tribe.id)
            } verifyAsync {
                adjectiveTextInput.getAttribute("value").await()
                    .assertIsNotEqualTo("")
                nounTextInput.getAttribute("value").await()
                    .assertIsNotEqualTo("")
            }
        }
    }

}

abstract class PlayerConfigOnePlayerTest(val buildTribe: () -> Tribe, val buildPlayer: () -> Player) {
    fun testPlayerConfig(handler: suspend CoroutineScope.(Tribe, Player) -> Unit) = testAsync {
        val tribe = tribeProvider.await()
        val player = playerProvider.await()

        try {
            handler(tribe, player)
        } finally {
            checkLogs()
        }
    }

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