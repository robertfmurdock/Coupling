package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pin.PinCard.pinCard
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RProps
import react.ReactElement
import react.dom.a
import react.dom.div
import react.dom.h2
import react.dom.h3

object PinList : RComponent<PinListProps>(provider()), PinListBuilder

data class PinListProps(val tribe: Tribe, val pins: List<Pin>, val pathSetter: (String) -> Unit) : RProps

interface PinListBuilder : StyledComponentRenderer<PinListProps, SimpleStyle> {

    override val componentPath get() = "pin/PinList"

    override fun StyledRContext<PinListProps, SimpleStyle>.render(): ReactElement {
        val (tribe, pins, pathSetter) = props
        return reactElement {
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
                    a(classes = "large orange button", href = "/${tribe.id.value}/pin/new") {
                        +"Add a new pin."
                    }
                }
            }
        }
    }
}

