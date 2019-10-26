package com.zegreatrob.coupling.core.entity.pairassignmentdocument

import com.zegreatrob.coupling.core.entity.pin.Pin

interface PinAssignmentSyntax {
    fun List<CouplingPair>.assign(pins: List<Pin>) =
            map { PinnedCouplingPair(it.asArray().toList().map { player -> player.withPins(pins) }) }
}
