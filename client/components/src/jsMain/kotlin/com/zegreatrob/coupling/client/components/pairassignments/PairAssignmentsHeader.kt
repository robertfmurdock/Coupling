package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props

external interface PairAssignmentsHeaderProps : Props {
    var pairAssignments: PairAssignmentDocument
}

@ReactFunc
val PairAssignmentsHeader by nfc<PairAssignmentsHeaderProps> { (pairAssignments) ->
    PairAssignmentBlock {
        +"Couples for ${pairAssignments.dateText()}"
    }
}
