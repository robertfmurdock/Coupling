package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import react.RBuilder
import react.RProps
import react.dom.div

data class PairAssignmentsHeaderProps(val pairAssignments: PairAssignmentDocument) : RProps

object PairAssignmentsHeader : FRComponent<PairAssignmentsHeaderProps>(provider()) {

    private val styles = useStyles("pairassignments/PairAssignmentsHeader")

    fun RBuilder.pairAssignmentsHeader(pairAssignments: PairAssignmentDocument) = child(
        PairAssignmentsHeader.component.rFunction,
        PairAssignmentsHeaderProps(pairAssignments)
    )

    override fun render(props: PairAssignmentsHeaderProps) = reactElement {
        val pairAssignments = props.pairAssignments
        div(classes = styles.className) {
            +"Couples for ${pairAssignments.dateText()}"
        }
    }
}