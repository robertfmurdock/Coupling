package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.Display
import csstype.FontWeight
import csstype.px
import emotion.react.css
import react.dom.html.ReactHTML.div

data class PairAssignmentsHeader(val pairAssignments: PairAssignmentDocument) :
    DataPropsBind<PairAssignmentsHeader>(pairAssignmentsHeader)

val pairAssignmentsHeader = tmFC<PairAssignmentsHeader> { (pairAssignments) ->
    div {
        css {
            display = Display.inlineBlock
            fontSize = 28.px
            fontWeight = FontWeight.bold
            borderRadius = 15.px
            paddingLeft = 40.px
            paddingRight = 5.px
            paddingBottom = 6.px
        }
        +"Couples for ${pairAssignments.dateText()}"
    }
}
