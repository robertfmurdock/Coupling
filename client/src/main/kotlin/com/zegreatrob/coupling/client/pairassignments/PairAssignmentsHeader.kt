package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

data class PairAssignmentsHeader(val pairAssignments: PairAssignmentDocument) :
    DataPropsBind<PairAssignmentsHeader>(pairAssignmentsHeader)

private val styles = useStyles("pairassignments/PairAssignmentsHeader")

val pairAssignmentsHeader = tmFC<PairAssignmentsHeader> { (pairAssignments) ->
    div {
        className = styles.className
        +"Couples for ${pairAssignments.dateText()}"
    }
}
