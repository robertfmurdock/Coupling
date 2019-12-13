package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.Pin

interface PinAssignmentSyntax {
    fun List<CouplingPair>.assign(pins: List<Pin>) =
        map {
            PinnedCouplingPair(it.asArray().toList().map { player ->
                player.withPins(
                    pins
                )
            })
        }
}
