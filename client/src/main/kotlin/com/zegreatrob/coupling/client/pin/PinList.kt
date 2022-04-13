package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.router.dom.Link

data class PinList(val tribe: Party, val pins: List<Pin>) : DataPropsBind<PinList>(pinList)

private val styles = useStyles("pin/PinList")

val pinList = tmFC<PinList> { (tribe, pins) ->
    div {
        className = styles.className
        ConfigHeader {
            this.tribe = tribe
            +"These are your pins."
        }
        h2 { +"There are many like them, but these are yours." }

        if (pins.isEmpty()) {
            +"Of course, you don't have any yet. If you'd like one, click that button below."
        }

        div {
            pins.map { child(PinCard(tribeId = tribe.id, pin = it, shouldLink = true), key = it.id) }
        }
        div {
            Link {
                to = "/${tribe.id.value}/pin/new"
                tabIndex = -1
                draggable = false
                child(CouplingButton(large, orange)) {
                    +"Add a new pin."
                }
            }
        }
    }
}
