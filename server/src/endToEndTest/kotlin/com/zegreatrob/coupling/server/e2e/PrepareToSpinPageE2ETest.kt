package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
import kotlin.test.Test

class PrepareToSpinPageE2ETest {

    companion object {

        private val beforeAllProvider by lazyDeferred {
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

        private val pinTribeSetup: TestTemplate<FullTribeData> = e2eSetup.extend(beforeAll = {
            val (tribe, players, pin) = beforeAllProvider.await()
            val sdk = sdkProvider.await()
            FullTribeData(players, listOf(pin), tribe, sdk)
        })

    }

    @Test
    fun withNoHistory() = pinTribeSetup(contextProvider = object : FullTribeContext() {
        val page = PrepareToSpinPage
    }.attach()) exercise {
        page.goTo(tribe.id)
    } verify {
        PlayerCard.playerElements.map { it.getText() }.await().toList()
            .assertIsEqualTo(players.map(Player::name))
    }

    @Test
    fun spinningWithAllPlayersOnWillGetAllPlayersBack() = pinTribeSetup(contextProvider = object : FullTribeContext() {
        val page = PrepareToSpinPage
    }.attach()) {
        page.goTo(tribe.id)
    } exercise {
        page.spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        CurrentPairAssignmentPage.assignedPairElements.count().await()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenTwoPlayersAreDisabledSpinWillYieldOnePairAndSavingPersistsThePair() =
        pinTribeSetup(contextProvider = FullTribeContext().attach()) {
            PrepareToSpinPage.goTo(tribe.id)
            with(PlayerCard) {
                playerElements.get(0).element(iconLocator).performClick()
                playerElements.get(2).element(iconLocator).performClick()
                playerElements.get(3).element(iconLocator).performClick()
            }
        } exercise {
            PrepareToSpinPage.spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verify {
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

    @Test
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = pinTribeSetup(contextProvider = object : FullTribeContext() {
        val page = PrepareToSpinPage
    }.attach()) {
        page.goTo(tribe.id)
        page.selectedPinElements.count().await()
            .assertIsEqualTo(1)
    } exercise {
        PrepareToSpinPage.spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        PinButton.pinElements.count().await()
            .assertIsEqualTo(1)
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() =
        pinTribeSetup(contextProvider = object : FullTribeContext() {
            val page = PrepareToSpinPage
        }.attach()) {
            page.goTo(tribe.id)
            page.selectedPinElements.performClick()
        } exercise {
            page.spinButton.performClick()
            CurrentPairAssignmentPage.waitForPage()
        } verify {
            PinButton.pinElements.count().await()
                .assertIsEqualTo(0)
        }
}
