package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.router.dom.Link

data class PinList(val tribe: Tribe, val pins: List<Pin>) : DataProps<PinList> {
    override val component: TMFC<PinList> get() = pinList
}

private val styles = useStyles("pin/PinList")

val pinList = tmFC<PinList> { (tribe, pins) ->
    div {
        className = styles.className
        div {
            className = styles["tribeBrowser"]
            child(TribeCard(tribe))
        }
        h2 { +"These are your pins." }
        h3 { +"There are many like them, but these are yours." }
        div {
            pins.map { child(PinCard(tribeId = tribe.id, pin = it, shouldLink = true), key = it.id) }
        }
        div {
            Link {
                to = "/${tribe.id.value}/pin/new"
                child(CouplingButton(large, orange, "", {}, {}) { +"Add a new pin." })
            }
        }
    }
}
