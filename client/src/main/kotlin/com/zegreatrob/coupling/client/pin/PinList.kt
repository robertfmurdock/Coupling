package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RProps
import react.ReactElement
import react.dom.a
import react.dom.div

object PinList : RComponent<PinListProps>(provider()), PinListBuilder

external interface PinListStyles {
    val pinListing: String
    val pin: String
    val pinIcon: String
}

data class PinListProps(val tribe: Tribe, val pins: List<Pin>) : RProps

interface PinListBuilder : StyledComponentRenderer<PinListProps, PinListStyles> {

    override val componentPath get() = "pin/PinList"

    override fun StyledRContext<PinListProps, PinListStyles>.render(): ReactElement {
        val (tribe, pins) = props
        return reactElement {
            div {
                div(classes = styles.pinListing) {
                    pins.map { child(PinCard(PinCardProps(tribe.id, it, true), key = it._id)) }
                    a(classes = "large orange button", href = "/${tribe.id.value}/pin/new") {
                        +"Add a new pin."
                    }
                }
            }
        }
    }
}

