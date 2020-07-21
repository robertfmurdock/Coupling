package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RProps
import react.dom.div

data class PairAssignmentsHeaderProps(val pairAssignments: PairAssignmentDocument) : RProps

private val styles = useStyles("pairassignments/PairAssignmentsHeader")

val PairAssignmentsHeader =
    reactFunction<PairAssignmentsHeaderProps> { (pairAssignments) ->
        div(classes = styles.className) {
            +"Couples for ${pairAssignments.dateText()}"
        }
    }

fun RBuilder.pairAssignmentsHeader(pairAssignments: PairAssignmentDocument) = child(
    PairAssignmentsHeader,
    PairAssignmentsHeaderProps(pairAssignments)
)
