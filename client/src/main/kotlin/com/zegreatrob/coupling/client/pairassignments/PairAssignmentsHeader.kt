package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

data class PairAssignmentsHeader(val pairAssignments: PairAssignmentDocument) : DataProps<PairAssignmentsHeader> {
    override val component: TMFC<PairAssignmentsHeader> get() = pairAssignmentsHeader
}

private val styles = useStyles("pairassignments/PairAssignmentsHeader")

val pairAssignmentsHeader = tmFC<PairAssignmentsHeader> { (pairAssignments) ->
    div {
        className = styles.className
        +"Couples for ${pairAssignments.dateText()}"
    }
}
