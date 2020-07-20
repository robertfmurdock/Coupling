package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.minreact.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RProps
import react.dom.button
import react.dom.div
import react.dom.h2
import react.dom.h3
import react.router.dom.routeLink

data class PinListProps(val tribe: Tribe, val pins: List<Pin>, val pathSetter: (String) -> Unit) : RProps

private val styles = useStyles("pin/PinList")

val PinList =
    reactFunction<PinListProps> { (tribe, pins, pathSetter) ->
        div(classes = styles.className) {
            div(classes = styles["tribeBrowser"]) {
                tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
            }
            h2 { +"These are your pins." }
            h3 { +"There are many like them, but these are yours." }
            div {
                pins.map { pinCard(tribeId = tribe.id, pin = it, shouldLink = true, key = it._id) }
            }
            div {
                routeLink(to = "/${tribe.id.value}/pin/new") {
                    button(classes = "large orange button") { +"Add a new pin." }
                }
            }
        }
    }
