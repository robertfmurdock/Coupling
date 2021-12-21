package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import react.dom.div
import react.dom.h2
import react.dom.h3
import react.router.dom.Link

data class PinListProps(val tribe: Tribe, val pins: List<Pin>) : DataProps

private val styles = useStyles("pin/PinList")

val PinList = reactFunction<PinListProps> { (tribe, pins) ->
        div(classes = styles.className) {
            div(classes = styles["tribeBrowser"]) {
                tribeCard(TribeCardProps(tribe))
            }
            h2 { +"These are your pins." }
            h3 { +"There are many like them, but these are yours." }
            div {
                pins.map { pinCard(tribeId = tribe.id, pin = it, shouldLink = true, key = it.id) }
            }
            div {
                Link {
                    attrs.to = "/${tribe.id.value}/pin/new"
                    couplingButton(large, orange) { +"Add a new pin." }
                }
            }
        }
    }
