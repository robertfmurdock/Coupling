package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.client.components.pin.PinCard
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.router.dom.Link
import web.cssom.Border
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.vh

external interface PinListProps : Props {
    var party: PartyDetails
    var pins: List<Pin>
}

@ReactFunc
val PinList by nfc<PinListProps> { (party, pins) ->
    div {
        css { pinListStyles() }
        ConfigHeader {
            this.party = party
            +"These are your pins."
        }
        h2 { +"There are many like them, but these are yours." }

        if (pins.isEmpty()) {
            +"Of course, you don't have any yet. If you'd like one, click that button below."
        }

        div {
            pins.map { PinCard(partyId = party.id, pin = it, shouldLink = true, key = it.id) }
        }
        div {
            Link {
                to = "/${party.id.value}/pin/new"
                tabIndex = -1
                draggable = false
                CouplingButton {
                    sizeRuleSet = large
                    colorRuleSet = orange
                    +"Add a new pin."
                }
            }
        }
    }
}

private fun PropertiesBuilder.pinListStyles() {
    display = Display.inlineBlock
    backgroundColor = Color("#FDF9EDFF")
    padding = 25.px
    minHeight = 100.vh
    border = Border(12.px, LineStyle.solid, NamedColor.black)
    borderTop = 2.px
    borderBottom = 2.px
    borderRadius = 82.px
    textAlign = TextAlign.left
}
