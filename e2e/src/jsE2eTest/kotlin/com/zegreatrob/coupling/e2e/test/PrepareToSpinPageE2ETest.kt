package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
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
                fire(SavePartyCommand(party))
                players.forEach { fire(SavePlayerCommand(party.id, it)) }
                sdk.await().fire(SavePinCommand(party.id, pin))
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

    // Tests related to Drag and Drop. WDIO support doesn't seem compatible with the react-dnd library, and these were original written as unit tests in Enzyme. Enzyme's gotta go, so they're here in limbo for now.
    // @Test
    // fun onPlayerDropWillTakeTwoPlayersAndSwapTheirPlaces() = pinPartySetup {
    //     PrepareToSpinPage.goTo(party.id)
    //     getSelectAllButton().click()
    //     getSpinButton().click()
    //     PairAssignmentsPage.waitForPage()
    // } exercise {
    //     val initialOrder = assignedPairElements.map { it.text() }
    //     assignedPairElements[0].all(playerLocator)[1]
    //         .dragAndDrop(
    //             assignedPairElements[2].all(playerLocator)[0],
    //         )
    //     initialOrder
    // } verify { initialOrder ->
    //     initialOrder.assertIsNotEqualTo(
    //         assignedPairElements.map { it.text() },
    //     )
    // }
    //
    // @Test
    // fun onPinDropWillTakeMovePinFromOnePairToAnother() = setup(object {
    //     val party = stubPartyDetails()
    //     val pin1 = stubPin()
    //     val pin2 = stubPin()
    //     val pair1 =
    //         pairOf(Player("1", name = "1", avatarType = null), Player("2", name = "2", avatarType = null)).withPins(
    //             setOf(pin1),
    //         )
    //     val pair2 =
    //         pairOf(Player("3", name = "3", avatarType = null), Player("4", name = "4", avatarType = null)).withPins(
    //             setOf(pin2),
    //         )
    //     val pairAssignments = PairAssignmentDocument(
    //         id = PairAssignmentDocumentId("${uuid4()}"),
    //         date = DateTime.now(),
    //         pairs = listOf(pair1, pair2),
    //     )
    //     var lastSetPairAssignments: PairAssignmentDocument? = null
    //     val wrapper = shallow(
    //         CurrentPairAssignmentsPanel(
    //             party,
    //             pairAssignments,
    //             { lastSetPairAssignments = it },
    //             dispatchFunc = StubDispatchFunc(),
    //             allowSave = false,
    //         ),
    //     )
    // }) exercise {
    //     pin1.dragTo(pair2, wrapper)
    // } verify {
    //     lastSetPairAssignments.assertNotNull { pairs ->
    //         pairs.pairs[0]
    //             .assertIsEqualTo(pair1.copy(pins = emptySet()))
    //         pairs.pairs[1]
    //             .assertIsEqualTo(pair2.copy(pins = setOf(pin2, pin1)))
    //     }
    // }
    //
    // @Test
    // fun onPlayerDropTheSwapWillNotLosePinAssignments() = setup(object {
    //     val party = stubPartyDetails()
    //     val player1 = Player("1", name = "1", avatarType = null)
    //     val player2 = Player("2", name = "2", avatarType = null)
    //     val player3 = Player("3", name = "3", avatarType = null)
    //     val player4 = Player("4", name = "4", avatarType = null)
    //
    //     val pin1 = stubPin()
    //     val pin2 = stubPin()
    //
    //     val pairAssignments = PairAssignmentDocument(
    //         id = PairAssignmentDocumentId("${uuid4()}"),
    //         date = DateTime.now(),
    //         pairs = listOf(
    //             pairOf(player1, player2).withPins(setOf(pin1)),
    //             pairOf(player3, player4).withPins(setOf(pin2)),
    //         ),
    //     )
    //     var lastSetPairAssignments: PairAssignmentDocument? = null
    //     val wrapper = shallow(
    //         CurrentPairAssignmentsPanel(
    //             party,
    //             pairAssignments,
    //             { lastSetPairAssignments = it },
    //             dispatchFunc = StubDispatchFunc(),
    //             allowSave = false,
    //         ),
    //     )
    // }) exercise {
    //     player2.dragTo(player3, wrapper)
    // } verify {
    //     lastSetPairAssignments.assertNotNull {
    //         it.pairs[0].pins
    //             .assertIsEqualTo(setOf(pin1))
    //         it.pairs[1].pins
    //             .assertIsEqualTo(setOf(pin2))
    //     }
    // }
    //
    // @Test
    // fun onPlayerDropWillNotSwapPlayersThatAreAlreadyPaired() = setup(object {
    //     val party = stubPartyDetails()
    //     val player1 = Player("1", name = "1", avatarType = null)
    //     val player2 = Player("2", name = "2", avatarType = null)
    //     val player3 = Player("3", name = "3", avatarType = null)
    //     val player4 = Player("4", name = "4", avatarType = null)
    //
    //     val pairAssignments = PairAssignmentDocument(
    //         id = PairAssignmentDocumentId("${uuid4()}"),
    //         date = DateTime.now(),
    //         pairs = listOf(
    //             pairOf(player1, player2),
    //             pairOf(player3, player4),
    //         ).withPins(),
    //     )
    //     var lastSetPairAssignments: PairAssignmentDocument? = null
    //     val wrapper = shallow(
    //         CurrentPairAssignmentsPanel(
    //             party,
    //             pairAssignments,
    //             { lastSetPairAssignments = it },
    //             dispatchFunc = StubDispatchFunc(),
    //             allowSave = false,
    //         ),
    //     )
    // }) exercise {
    //     player4.dragTo(player3, wrapper)
    // } verify {
    //     lastSetPairAssignments.assertNotNull {
    //         it.pairs[0].toPair().assertIsEqualTo(pairOf(player1, player2))
    //         it.pairs[1].toPair().assertIsEqualTo(pairOf(player3, player4))
    //     }
    // }

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
