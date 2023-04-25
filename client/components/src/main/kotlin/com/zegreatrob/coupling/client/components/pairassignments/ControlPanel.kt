package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.Margin
import web.cssom.px
import web.cssom.rgba

data class ControlPanel(val party: Party) : DataPropsBind<ControlPanel>(controlPanel)

val controlPanel by ntmFC<ControlPanel> { (party) ->
    div {
        div {
            css {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding = 5.px
                margin = Margin(5.px, 0.px)
                backgroundColor = Color("#d5cdc3")
                boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
            }
            div { add(PrepareToSpinButton(party)) }
        }
    }
}
