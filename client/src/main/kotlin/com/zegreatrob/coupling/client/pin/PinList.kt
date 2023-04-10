package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.components.ConfigHeader
import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.orange
import com.zegreatrob.coupling.components.pin.PinCard
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Border
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.NamedColor
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.px
import csstype.vh
import emotion.react.css
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.router.dom.Link

data class PinList(val party: Party, val pins: List<Pin>) : DataPropsBind<PinList>(pinList)

val pinList = tmFC<PinList> { (party, pins) ->
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
            pins.map {
                add(
                    PinCard(
                        partyId = party.id,
                        pin = it,
                        shouldLink = true,
                    ),
                    key = it.id,
                )
            }
        }
        div {
            Link {
                to = "/${party.id.value}/pin/new"
                tabIndex = -1
                draggable = false
                add(CouplingButton(large, orange)) {
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
