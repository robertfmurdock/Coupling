package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import stubPairAssignmentDoc
import stubPin
import stubPlayer
import kotlin.test.Test

class AssignPinsActionTest {

    companion object : AssignPinsActionDispatcher;

    @Test
    fun givenOnePinForAssigningToPairHasNeverBeenUsedWillAssignToFirstPair() = setup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val alternatePair = pairOf(stubPlayer(), stubPlayer())
        val pairs = listOf(expectedPair, alternatePair)
    }) exercise {
        AssignPinsAction(pairs, listOf(pin), emptyList()).perform()
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                expectedPair.withPins(listOf(pin)),
                alternatePair.withPins()
            )
        )
    }

    @Test
    fun givenTwoPinsForAssigningToPairHasNeverBeenUsedWillAssignToEachPair() = setup(object {
        val pins = listOf(
            stubPin().copy(target = PinTarget.Pair),
            stubPin().copy(target = PinTarget.Pair)
        )

        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val alternatePair = pairOf(stubPlayer(), stubPlayer())
        val pairs = listOf(expectedPair, alternatePair)
    }) exercise {
        AssignPinsAction(pairs, pins, emptyList()).perform()
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                expectedPair.withPins(listOf(pins[0])),
                alternatePair.withPins(listOf(pins[1]))
            )
        )
    }

    @Test
    fun givenTwoPinsAndOnlyOnePairWillAssignBothToThatPair() = setup(object {
        val pins = listOf(
            stubPin().copy(target = PinTarget.Pair),
            stubPin().copy(target = PinTarget.Pair)
        )

        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val pairs = listOf(expectedPair)
    }) exercise {
        AssignPinsAction(pairs, pins, emptyList()).perform()
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                expectedPair.withPins(listOf(pins[0], pins[1]))
            )
        )
    }

    @Test
    fun givenOnePinForAssigningToPairThatHasBeenUsedOnMemberOfFirstPairWillAssignToSecondPair() = setup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val alternatePair = pairOf(player1, player2)
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val expectedPair = pairOf(player3, player4)
        val pairs = listOf(alternatePair, expectedPair)

        val history = listOf(
            stubPairAssignmentDoc().copy(pairs = listOf(pairOf(player1).withPins(listOf(pin))))
        )
    }) exercise {
        AssignPinsAction(pairs, listOf(pin), history).perform()
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                alternatePair.withPins(),
                expectedPair.withPins(listOf(pin))
            )
        )
    }

    @Test
    fun givenOnePinForAssigningToPairThatHasBeenUsedOnMembersOfBothPairsWillAssignToFirstPair() = setup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val expectedPair = pairOf(player1, player2)
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val alternatePair = pairOf(player3, player4)
        val pairs = listOf(expectedPair, alternatePair)

        val history = listOf(
            stubPairAssignmentDoc().copy(pairs = listOf(pairOf(player1, player3).withPins(listOf(pin))))
        )
    }) exercise {
        AssignPinsAction(pairs, listOf(pin), history).perform()
    } verify { result ->
        result.assertIsEqualTo(
            listOf(
                expectedPair.withPins(listOf(pin)),
                alternatePair.withPins()
            )
        )
    }

    @Test
    fun willAssignNoPinsWhenThereAreNoPlayers() = setup(object {
        val pins = listOf(Pin(name = "Lucky"))
        val players = emptyList<CouplingPair>()
    }) exercise {
        AssignPinsAction(players, pins, emptyList()).perform()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}
