package com.zegreatrob.coupling.common.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.pin.Pin

interface PinAssignmentSyntax {
    fun List<CouplingPair>.assign(pins: List<Pin>) =
            map { PinnedCouplingPair(it.asArray().toList().map { player -> player.withPins(pins) }) }
}
