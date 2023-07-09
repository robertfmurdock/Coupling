package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.Margin
import web.cssom.px
import web.cssom.rgb

external interface ControlPanelProps : Props {
    var party: PartyDetails
}

@ReactFunc
val ControlPanel by nfc<ControlPanelProps> { (party) ->
    div {
        div {
            css {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding = 5.px
                margin = Margin(5.px, 0.px)
                backgroundColor = Color("#d5cdc3")
                boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
            }
            div { add(PrepareToSpinButton(party)) }
        }
    }
}
