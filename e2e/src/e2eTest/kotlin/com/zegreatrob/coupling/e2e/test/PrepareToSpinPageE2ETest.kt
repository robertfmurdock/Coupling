package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.e2e.test.AssignedPair.assignedPairElements
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.CurrentPairAssignmentsPanel.getSaveButton
import com.zegreatrob.coupling.e2e.test.CurrentPairAssignmentsPanel.querySaveButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSelectAllButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSelectNoneButton
import com.zegreatrob.coupling.e2e.test.PrepareToSpinPage.getSpinButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
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
            val sdk = sdk.await().apply {
                perform(SavePartyCommand(party))
                players.forEach { perform(SavePlayerCommand(party.id, it)) }
                perform(SavePinCommand(party.id, pin))
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

        private fun buildFunkyParty() = PartyDetails(
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
