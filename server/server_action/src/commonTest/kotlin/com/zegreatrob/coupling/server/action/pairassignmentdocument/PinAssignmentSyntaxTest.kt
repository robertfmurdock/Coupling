package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinAssignmentSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import stubPin
import stubPlayer
import kotlin.test.Test

class PinAssignmentSyntaxTest {

    companion object : PinAssignmentSyntax;

    @Test
    fun givenOnePinForAssigningToPairHasNeverBeenUsedWillAssignToFirstPair() = setup(object {
        val pin = stubPin().copy(target = PinTarget.Pair)
        val expectedPair = pairOf(stubPlayer(), stubPlayer())
        val alternatePair = pairOf(stubPlayer(), stubPlayer())
        val pairs = listOf(expectedPair, alternatePair)
    }) exercise {
        pairs.assign(listOf(pin))
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
        players.assign(pins)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}
