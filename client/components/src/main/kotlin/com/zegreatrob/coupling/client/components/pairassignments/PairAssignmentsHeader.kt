package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.PairAssignmentBlock
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC

data class PairAssignmentsHeader(val pairAssignments: PairAssignmentDocument) :
    DataPropsBind<PairAssignmentsHeader>(pairAssignmentsHeader)

val pairAssignmentsHeader by ntmFC<PairAssignmentsHeader> { (pairAssignments) ->
    PairAssignmentBlock {
        +"Couples for ${pairAssignments.dateText()}"
    }
}
