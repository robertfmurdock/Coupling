package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.BoxShadow
import csstype.Color
import csstype.Display
import csstype.Margin
import csstype.px
import csstype.rgba
import emotion.react.css
import react.dom.html.ReactHTML.div

data class ControlPanel(val party: Party) : DataPropsBind<ControlPanel>(controlPanel)

val controlPanel = tmFC<ControlPanel> { (party) ->
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
