package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.CurrentPairAssignmentPage.saveButton
import com.zegreatrob.coupling.server.e2e.PrepareToSpinPage.selectAllButton
import com.zegreatrob.coupling.server.e2e.PrepareToSpinPage.selectNoneButton
import com.zegreatrob.coupling.server.e2e.PrepareToSpinPage.spinButton
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke
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
        selectAllButton.performClick()
    } exercise {
        spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        CurrentPairAssignmentPage.assignedPairElements.count()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenTwoPlayersAreEnabledSpinWillYieldOnePairAndSavingPersistsThePair() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        with(PlayerCard) {
            selectNoneButton.performClick()
            playerElements.get(1).element(iconLocator).performClick()
            playerElements.get(4).element(iconLocator).performClick()
        }
    } exercise {
        spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        CurrentPairAssignmentPage.assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.playerElements.count()
            .assertIsEqualTo(3)

        saveButton.performClick()
        CurrentPairAssignmentPage.waitForSaveButtonToNotBeDisplayed()

        CurrentPairAssignmentPage.assignedPairElements.count()
            .assertIsEqualTo(1)
        PlayerRoster.playerElements.count()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.getSelectedPinElements().count()
            .assertIsEqualTo(1)
    } exercise {
        spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(1)
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.getSelectedPinElements().get(0).performClick()
    } exercise {
        spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        PinButton.pinElements.count()
            .assertIsEqualTo(0)
    }

}
