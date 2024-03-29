package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class AssignPinsActionTest {

    companion object : AssignPinsAction.Dispatcher

    @Test
    fun givenOnePinForAssigningToPairHasNeverBeenUsedWillAssignToFirstPair() = asyncSetup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val alternatePair = pairOf(stubPlayer(), stubPlayer())
        val pairs = notEmptyListOf(expectedPair, alternatePair)
    }) exercise {
        perform(
            AssignPinsAction(
                pairs,
                listOf(pin),
                emptyList(),
            ),
        )
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                expectedPair.withPins(setOf(pin)),
                alternatePair.withPins(),
            ),
        )
    }

    @Test
    fun givenTwoPinsForAssigningToPairHasNeverBeenUsedWillAssignToEachPair() = asyncSetup(object {
        val pins = listOf(
            stubPin().copy(target = PinTarget.Pair),
            stubPin().copy(target = PinTarget.Pair),
        )

        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val alternatePair = pairOf(stubPlayer(), stubPlayer())
        val pairs = notEmptyListOf(expectedPair, alternatePair)
    }) exercise {
        perform(
            AssignPinsAction(
                pairs,
                pins,
                emptyList(),
            ),
        )
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                expectedPair.withPins(setOf(pins[0])),
                alternatePair.withPins(setOf(pins[1])),
            ),
        )
    }

    @Test
    fun givenTwoPinsAndOnlyOnePairWillAssignBothToThatPair() = asyncSetup(object {
        val pins = listOf(
            stubPin().copy(target = PinTarget.Pair),
            stubPin().copy(target = PinTarget.Pair),
        )
        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val pairs = notEmptyListOf(expectedPair)
    }) exercise {
        perform(
            AssignPinsAction(
                pairs,
                pins,
                emptyList(),
            ),
        )
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                expectedPair.withPins(setOf(pins[0], pins[1])),
            ),
        )
    }

    @Test
    fun givenOnePinForAssigningToPairThatHasBeenUsedOnMemberOfFirstPairWillAssignToSecondPair() = asyncSetup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val alternatePair = pairOf(player1, player2)
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val expectedPair = pairOf(player3, player4)
        val pairs = notEmptyListOf(alternatePair, expectedPair)

        val history = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player1).withPins(setOf(pin)))),
        )
    }) exercise {
        perform(
            AssignPinsAction(
                pairs,
                listOf(pin),
                history,
            ),
        )
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                alternatePair.withPins(),
                expectedPair.withPins(setOf(pin)),
            ),
        )
    }

    @Test
    fun givenOnePinForAssigningToPairThatHasBeenUsedOnMembersOfBothPairsWillAssignToFirstPair() = asyncSetup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val expectedPair = pairOf(player1, player2)
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val alternatePair = pairOf(player3, player4)
        val pairs = notEmptyListOf(expectedPair, alternatePair)

        val history = listOf(
            stubPairAssignmentDoc().copy(pairs = notEmptyListOf(pairOf(player1, player3).withPins(setOf(pin)))),
        )
    }) exercise {
        perform(AssignPinsAction(pairs, listOf(pin), history))
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                expectedPair.withPins(setOf(pin)),
                alternatePair.withPins(),
            ),
        )
    }

    @Test
    fun givenTwoPinsWillPreferToDistributePins() = asyncSetup(object {
        val pin1 = stubPin().copy(target = PinTarget.Pair)
        val pin2 = stubPin().copy(target = PinTarget.Pair)
        val player1 = stubPlayer()
        val player2 = stubPlayer()
        val expectedPair = pairOf(player1, player2)
        val player3 = stubPlayer()
        val player4 = stubPlayer()
        val alternatePair = pairOf(player3, player4)
        val pairs = notEmptyListOf(expectedPair, alternatePair)

        val history = emptyList<PairAssignmentDocument>()
    }) exercise {
        perform(
            AssignPinsAction(
                pairs,
                listOf(pin1, pin2),
                history,
            ),
        )
    } verify { result ->
        result.assertIsEqualTo(
            notEmptyListOf(
                expectedPair.withPins(setOf(pin1)),
                alternatePair.withPins(setOf(pin2)),
            ),
        )
    }
}
