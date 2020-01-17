package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget

interface PinAssignmentSyntax {
    fun List<CouplingPair>.assign(pins: List<Pin>): List<PinnedCouplingPair> {
        var pairs = map { it.withPins() }

        pins.filter { it.target == PinTarget.Pair }
            .forEach { pin ->
                val pinIterator = listOf(pin).iterator()
                pairs = pairs.map { pair ->
                    if (pair.pins.isEmpty() && pinIterator.hasNext()) {
                        pair.copy(pins = listOf(pinIterator.next()))
                    } else {
                        pair
                    }
                }
            }

        return pairs
    }
}
