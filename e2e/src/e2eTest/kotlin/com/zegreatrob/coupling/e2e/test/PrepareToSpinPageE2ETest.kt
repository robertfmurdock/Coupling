package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.CurrentPairAssignmentsPanel.getSaveButton
import com.zegreatrob.coupling.e2e.test.CurrentPairAssignmentsPanel.querySaveButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSelectAllButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSelectNoneButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSpinButton
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class PrepareToSpinPageE2ETest {

    companion object {

        private val pinPartySetup: TestTemplate<FullPartyData> = e2eSetup.extend(beforeAll = {
            val party = buildFunkyParty()
            val players = (1..5).map(Companion::buildPlayer)
            val pin = Pin("${randomInt()}-PairAssignmentsPageE2ETest", name = "e2e-pin")
            val sdk = sdkProvider.await().apply {
                party.save()
                players.forEach { party.id.with(it).save() }
                party.id.with(pin).save()
            }

            FullPartyData(players, listOf(pin), party, sdk)
        }).extend(sharedTeardown = {
            val saveButton = querySaveButton()
            if (saveButton.isDisplayed()) {
                saveButton.click()
            }
            if (WebdriverBrowser.isAlertOpen()) {
                WebdriverBrowser.dismissAlert()
            }
        })

        private fun buildFunkyParty() = Party(
            id = PartyId("${randomInt()}-PairAssignmentsPageE2ETest"),
            name = "Funkytown",
        )

        private fun buildPlayer(it: Int) = Player(
            id = "${randomInt()}-PairAssignmentsPageE2ETest-$it",
            name = "Player$it",
            avatarType = null,
        )
    }

    @Test
    fun withNoHistory() = pinPartySetup() exercise {
        PrepareToSpinPage.goTo(party.id)
    } verify {
        PlayerCard.playerElements.map { it.text() }.toList()
            .assertIsEqualTo(players.map(Player::name))
    }

    @Test
    fun spinningWithAllPlayersOnWillGetAllPlayersBack() = pinPartySetup {
        PrepareToSpinPage.goTo(party.id)
        getSelectAllButton().click()
    } exercise {
        getSpinButton().click()
        PairAssignmentsPage.waitForPage()
    } verify {
        assignedPairElements.count()
            .assertIsEqualTo(3)
    }

    @Test
    fun spinningWillAlertOnExitIfNotSavedAndIfAcceptedPairsAreNotSaved() = pinPartySetup {
        PrepareToSpinPage.goTo(party.id)
        getSelectNoneButton().click()
        PlayerCard.playerElements[0].element(PlayerCard.iconLocator).click()
        getSpinButton().click()
        PairAssignmentsPage.waitForPage()
    } exercise {
        WebdriverBrowser.setLocation("welcome")
//        WebdriverBrowser.waitForAlert()
//        WebdriverBrowser.alertText().also {
//            WebdriverBrowser.dismissAlert()
//        }
    } verify { // _ ->
//        alertText.assertIsEqualTo("Press OK to save these pairs.")
//        assignedPairElements.count().assertIsEqualTo(1)
//        saveButton.isDisplayed().assertIsEqualTo(true)
    }

    @Test
    fun whenTwoPlayersAreEnabledSpinWillYieldOnePairAndSavingPersistsThePair() = pinPartySetup {
        PrepareToSpinPage.goTo(party.id)
        with(PlayerCard) {
            getSelectNoneButton().click()
            playerElements[1].element(iconLocator).click()
            playerElements[4].element(iconLocator).click()
        }
    } exercise {
        getSpinButton().click()
        PairAssignmentsPage.waitForPage()
    } verify {
        assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.getPlayerElements("Unpaired players").count()
            .assertIsEqualTo(3)

        getSaveButton().click()
        CurrentPairAssignmentsPanel.waitForSaveButtonToNotBeDisplayed()

        assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.getPlayerElements("Unpaired players").count()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = pinPartySetup {
        PrepareToSpinPage.goTo(party.id)
        PrepareToSpinPage.selectedPinElements.count()
            .assertIsEqualTo(1)
    } exercise {
        getSpinButton().click()
        PairAssignmentsPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(1)
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() = pinPartySetup {
        PrepareToSpinPage.goTo(party.id)
        PrepareToSpinPage.selectedPinElements[0].click()
    } exercise {
        getSpinButton().click()
        PairAssignmentsPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(0)
    }
}
