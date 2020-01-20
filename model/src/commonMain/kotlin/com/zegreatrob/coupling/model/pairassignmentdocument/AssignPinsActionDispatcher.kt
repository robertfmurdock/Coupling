package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget


data class AssignPinsAction(val pairs: List<CouplingPair>, val pins: List<Pin>)

interface AssignPinsActionDispatcher {
    fun AssignPinsAction.perform(): List<PinnedCouplingPair> {
        var pinnedPairs = pairs.map { it.withPins() }

        pins.filter { it.target == PinTarget.Pair }
            .forEach { pin ->
                val pinIterator = listOf(pin).iterator()
                pinnedPairs = pinnedPairs.map { pair ->
                    if (pair.pins.isEmpty() && pinIterator.hasNext()) {
                        pair.copy(pins = listOf(pinIterator.next()))
                    } else {
                        pair
                    }
                }
            }

        return pinnedPairs
    }
}
