package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.InputType
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

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
                div(classes = styles.pinListing) { pins.map { pin(it, styles) } }
                a(classes = "large orange button", href = "/${tribe.id.value}/pin/new") {
                    +"Add a new pin."
                }
            }
        }
    }

    private fun RBuilder.pin(pin: Pin, styles: PinListStyles) = span(classes = styles.pin) {
        i(classes = styles.pinIcon) {
            attrs {
                classes += setOf(
                    "fa",
                    "fa-fw",
                    "fa-d2",
                    "fa-2x",
                    pin.icon ?: ""
                )
            }
        }
        input(type = InputType.text) { attrs { value = pin.name ?: "" } }
        input(type = InputType.text) { attrs { value = pin.icon ?: "" } }
    }
}

