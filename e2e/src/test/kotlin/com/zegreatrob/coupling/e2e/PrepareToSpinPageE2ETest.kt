package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.CurrentPairAssignmentsPanel.saveButton
import com.zegreatrob.coupling.e2e.PrepareToSpinPage.selectAllButton
import com.zegreatrob.coupling.e2e.PrepareToSpinPage.selectNoneButton
import com.zegreatrob.coupling.e2e.PrepareToSpinPage.spinButton
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class PrepareToSpinPageE2ETest {

    companion object {

        private val pinTribeSetup: TestTemplate<FullTribeData> = e2eSetup.extend(beforeAll = {
            val tribe = buildFunkyTribe()
            val players = (1..5).map(::buildPlayer)
            val pin = Pin("${randomInt()}-PairAssignmentsPageE2ETest", name = "e2e-pin")
            val sdk = sdkProvider.await()
            sdk.save(tribe)
            players.forEach { sdk.save(tribe.id.with(it)) }
            sdk.save(tribe.id.with(pin))

            FullTribeData(players, listOf(pin), tribe, sdk)
        }).extend(sharedTeardown = {
            if (saveButton.isDisplayed()) {
                saveButton.click()
            }
            if (WebdriverBrowser.isAlertOpen())
                WebdriverBrowser.dismissAlert()
        })

        private fun buildFunkyTribe() = Tribe(
            id = TribeId("${randomInt()}-PairAssignmentsPageE2ETest"),
            name = "Funkytown"
        )

        private fun buildPlayer(it: Int) = Player(
            id = "${randomInt()}-PairAssignmentsPageE2ETest-${it}",
            name = "Player$it"
        )

    }

    @Test
    fun withNoHistory() = pinTribeSetup() exercise {
        PrepareToSpinPage.goTo(tribe.id)
    } verify {
        PlayerCard.playerElements.map { it.text() }.toList()
            .assertIsEqualTo(players.map(Player::name))
    }

    @Test
    fun spinningWithAllPlayersOnWillGetAllPlayersBack() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        selectAllButton.click()
    } exercise {
        spinButton.click()
        PairAssignmentsPage.waitForPage()
    } verify {
        assignedPairElements.count()
            .assertIsEqualTo(3)
    }

    @Test
    fun spinningWillAlertOnExitIfNotSavedAndIfAcceptedPairsAreNotSaved() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        selectNoneButton.click()
        PlayerCard.playerElements.get(0).element(PlayerCard.iconLocator).click()
        spinButton.click()
        PairAssignmentsPage.waitForPage()
    } exercise {
        WebdriverBrowser.setLocation("/welcome")
        WebdriverBrowser.waitForAlert()
        WebdriverBrowser.alertText().also {
            WebdriverBrowser.dismissAlert()
        }
    } verify { alertText ->
        alertText.assertIsEqualTo("Press OK to save these pairs.")
        assignedPairElements.count().assertIsEqualTo(1)
        saveButton.isDisplayed().assertIsEqualTo(true)
    }

    @Test
    fun whenTwoPlayersAreEnabledSpinWillYieldOnePairAndSavingPersistsThePair() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        with(PlayerCard) {
            selectNoneButton.click()
            playerElements.get(1).element(iconLocator).click()
            playerElements.get(4).element(iconLocator).click()
        }
    } exercise {
        spinButton.click()
        PairAssignmentsPage.waitForPage()
    } verify {
        assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.playerElements.count()
            .assertIsEqualTo(3)

        saveButton.click()
        CurrentPairAssignmentsPanel.waitForSaveButtonToNotBeDisplayed()

        assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.playerElements.count()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.selectedPinElements.count()
            .assertIsEqualTo(1)
    } exercise {
        spinButton.click()
        PairAssignmentsPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(1)
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.selectedPinElements.get(0).click()
    } exercise {
        spinButton.click()
        PairAssignmentsPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(0)
    }

}
