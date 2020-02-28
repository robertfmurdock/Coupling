package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.test.Test

class PrepareToSpinPageE2ETest {

    companion object {
        fun testPairAssignments(handler: suspend (Tribe, List<Player>, Pin) -> Unit) = testAsync {
            val (tribe, players, pin) = beforeAllProvider.await()
            CouplingLogin.loginProvider.await()
            handler(tribe, players, pin)
        }

        val beforeAllProvider by lazyDeferred {
            val tribe = Tribe(
                id = TribeId("${randomInt()}-PairAssignmentsPageE2ETest"),
                name = "Funkytown"
            )
            val players = (1..5).map {
                Player(
                    id = "${randomInt()}-PairAssignmentsPageE2ETest-${it}",
                    name = "Player$it"
                )
            }
            val pin = Pin("${randomInt()}-PairAssignmentsPageE2ETest", name = "e2e-pin")
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            players.forEach { sdk.save(tribe.id.with(it)) }
            sdk.save(tribe.id.with(pin))

            Triple(tribe, players, pin)
        }
    }

    @Test
    fun withNoHistory() = testPairAssignments { tribe, players, _ ->
        setupAsync(PrepareToSpinPage) exerciseAsync {
            goTo(tribe.id)
        } verifyAsync {
            PlayerCard.playerElements.map { it.getText() }.await().toList()
                .assertIsEqualTo(players.map(Player::name))
        }
    }

    @Test
    fun spinningWithAllPlayersOnWillGetAllPlayersBack() = testPairAssignments { tribe, _, _ ->
        setupAsync(PrepareToSpinPage) {
            goTo(tribe.id)
        } exerciseAsync {
            spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verifyAsync {
            CurrentPairAssignmentPage.assignedPairElements.count().await()
                .assertIsEqualTo(3)
        }
    }

    @Test
    fun whenTwoPlayersAreDisabledSpinWillYieldOnePairAndSavingPersistsThePair() = testPairAssignments { tribe, _, _ ->
        setupAsync(object {}) {
            PrepareToSpinPage.goTo(tribe.id)
            with(PlayerCard) {
                playerElements.get(0).element(iconLocator).performClick()
                playerElements.get(2).element(iconLocator).performClick()
                playerElements.get(3).element(iconLocator).performClick()
            }
        } exerciseAsync {
            PrepareToSpinPage.spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verifyAsync {
            CurrentPairAssignmentPage.assignedPairElements.count().await()
                .assertIsEqualTo(1)
            PlayerRoster.playerElements.count().await()
                .assertIsEqualTo(3)

            CurrentPairAssignmentPage.saveButton.performClick()
            CurrentPairAssignmentPage.waitForSaveButtonToNotBeDisplayed()

            CurrentPairAssignmentPage.assignedPairElements.count().await()
                .assertIsEqualTo(1)
            PlayerRoster.playerElements.count().await()
                .assertIsEqualTo(3)
        }
    }

    @Test
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = testPairAssignments { tribe, _, _ ->
        setupAsync(object {
        }) {
            PrepareToSpinPage.goTo(tribe.id)
            PrepareToSpinPage.selectedPinElements.count().await()
                .assertIsEqualTo(1)
        } exerciseAsync {
            PrepareToSpinPage.spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verifyAsync {
            PinButton.pinElements.count().await()
                .assertIsEqualTo(1)
        }
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() = testPairAssignments { tribe, _, _ ->
        setupAsync(object {
        }) {
            PrepareToSpinPage.goTo(tribe.id)
            PrepareToSpinPage.selectedPinElements.performClick()
        } exerciseAsync {
            PrepareToSpinPage.spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verifyAsync {
            PinButton.pinElements.count().await()
                .assertIsEqualTo(0)
        }
    }

}
