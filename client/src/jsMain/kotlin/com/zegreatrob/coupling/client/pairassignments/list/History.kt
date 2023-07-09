package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignmentRow
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import web.cssom.Border
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Padding
import web.cssom.px
import web.cssom.vh

data class History(
    val party: PartyDetails,
    val history: List<PairAssignmentDocument>,
    val controls: Controls<DeletePairAssignmentsCommand.Dispatcher>,
    val windowFunctions: WindowFunctions = WindowFunctions,
) : DataPropsBind<History>(com.zegreatrob.coupling.client.pairassignments.list.history)

private val history by ntmFC<History> { (party, history, controls, windowFuncs) ->
    div {
        css {
            display = Display.inlineBlock
            backgroundColor = Color("#dae8e0")
            padding = Padding(0.px, 25.px, 25.px, 25.px)
            minHeight = 100.vh
            border = Border(12.px, LineStyle.solid, Color("#4f5853"))
            borderTop = 2.px
            borderBottom = 2.px
            borderRadius = 82.px
        }
        ConfigHeader {
            this.party = party
            +"History!"
        }
        span {
            css {
                display = Display.inlineBlock
            }
            history.forEach {
                add(PairAssignmentRow(party, it, controls, windowFuncs))
            }
        }
    }
}