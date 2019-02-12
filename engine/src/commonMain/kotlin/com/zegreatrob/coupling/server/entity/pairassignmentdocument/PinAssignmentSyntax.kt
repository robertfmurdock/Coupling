package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.withPins
import com.zegreatrob.coupling.common.entity.pin.Pin

interface PinAssignmentSyntax {
    fun List<CouplingPair>.assign(pins: List<Pin>) =
            map { PinnedCouplingPair(it.asArray().toList().map { player -> player.withPins(pins) }) }
}
