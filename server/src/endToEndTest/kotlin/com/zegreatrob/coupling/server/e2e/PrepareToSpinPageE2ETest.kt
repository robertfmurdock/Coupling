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
        PlayerCard.playerElements.map { it.getText() }.await().toList()
            .assertIsEqualTo(players.map(Player::name))
    }

    @Test
    fun spinningWithAllPlayersOnWillGetAllPlayersBack() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.selectAllButton.performClick()
    } exercise {
        PrepareToSpinPage.spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        CurrentPairAssignmentPage.assignedPairElements.count().await()
            .assertIsEqualTo(3)
    }

    @Test
    fun whenTwoPlayersAreEnabledSpinWillYieldOnePairAndSavingPersistsThePair() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        with(PlayerCard) {
            PrepareToSpinPage.selectNoneButton.performClick()
            playerElements.get(1).element(iconLocator).performClick()
            playerElements.get(4).element(iconLocator).performClick()
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
    fun whenPinIsEnabledSpinWillIncludePinInAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.selectedPinElements.count().await()
            .assertIsEqualTo(1)
    } exercise {
        PrepareToSpinPage.spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        PinButton.pinElements.count().await()
            .assertIsEqualTo(1)
    }

    @Test
    fun whenPinIsDisabledSpinWillExcludePinFromAssignment() = pinTribeSetup {
        PrepareToSpinPage.goTo(tribe.id)
        PrepareToSpinPage.selectedPinElements.performClick()
    } exercise {
        PrepareToSpinPage.spinButton.performClick()
        CurrentPairAssignmentPage.waitForPage()
    } verify {
        PinButton.pinElements.count().await()
            .assertIsEqualTo(0)
    }

}
