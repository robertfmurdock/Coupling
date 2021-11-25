package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.reactFunction
import react.Props
import react.RBuilder
import react.dom.div

data class PairAssignmentsHeaderProps(val pairAssignments: PairAssignmentDocument) : Props

private val styles = useStyles("pairassignments/PairAssignmentsHeader")

val PairAssignmentsHeader = reactFunction<PairAssignmentsHeaderProps> { (pairAssignments) ->
    div(classes = styles.className) {
        +"Couples for ${pairAssignments.dateText()}"
    }
}

fun RBuilder.pairAssignmentsHeader(pairAssignments: PairAssignmentDocument) = child(
    PairAssignmentsHeader,
    PairAssignmentsHeaderProps(pairAssignments)
)
