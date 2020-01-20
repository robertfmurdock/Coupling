package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
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
        AssignPinsAction(pairs, listOf(pin)).perform()
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
        AssignPinsAction(players, pins).perform()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}
