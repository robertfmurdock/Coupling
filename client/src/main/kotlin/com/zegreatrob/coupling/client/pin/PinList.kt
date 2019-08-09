package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.StyledComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.InputType
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.*

object PinList : ComponentProvider<PinListProps>(), PinListBuilder

external interface PinListStyles {
    val pinListing: String
}

data class PinListProps(val tribe: KtTribe, val pins: List<Pin>) : RProps

interface PinListBuilder : StyledComponentBuilder<PinListProps, PinListStyles> {

    override val componentPath: String get() = "pin/PinList"

    override fun build() = buildBy {
        val (tribe, pins) = props
        {
            div {
                div(classes = styles.pinListing) { pins.map { pin(it) } }
                a(classes = "large orange button", href = "/${tribe.id.value}/pin/new") {
                    +"Add a new pin."
                }
            }
        }
    }

    private fun RBuilder.pin(pin: Pin) = span(classes = "pin") {
        i {
            attrs {
                classes = setOf(
                    "pin-icon",
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

