package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC

data class PairAssignmentsHeader(val pairAssignments: PairAssignmentDocument) :
    DataPropsBind<PairAssignmentsHeader>(pairAssignmentsHeader)

val pairAssignmentsHeader = tmFC<PairAssignmentsHeader> { (pairAssignments) ->
    PairAssignmentBlock {
        +"Couples for ${pairAssignments.dateText()}"
    }
}
