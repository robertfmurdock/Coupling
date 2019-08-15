package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.InputType
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.*

object PinList : ComponentProvider<PinListProps>(provider()), PinListBuilder

external interface PinListStyles {
    val pinListing: String
    val pin: String
    val pinIcon: String
}

data class PinListProps(val tribe: KtTribe, val pins: List<Pin>) : RProps

interface PinListBuilder : StyledComponentBuilder<PinListProps, PinListStyles> {

    override val componentPath: String get() = "pin/PinList"

    override fun build() = buildBy {
        val (tribe, pins) = props
        reactElement {
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

