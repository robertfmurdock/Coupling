package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ComponentBuilder
import com.zegreatrob.coupling.client.ComponentProvider
import com.zegreatrob.coupling.client.styledComponent
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.id
import react.RBuilder
import react.RProps
import react.dom.*

object PinList : ComponentProvider<PinListProps>(), PinListBuilder

val RBuilder.pinList get() = PinList.captor(this)

external interface PinListStyles

data class PinListProps(val tribe: KtTribe, val pins: List<Pin>) : RProps

interface PinListBuilder : ComponentBuilder<PinListProps> {
    override fun build() = styledComponent<PinListProps, PinListStyles>("pin/PinList")
    { props, styles ->
        val (tribe, pins) = props
        div(classes = "pin-list-frame") {
            div {
                attrs { id = "pin-listing" }
                pins.map { pin(it) }
            }
            a(classes = "large orange button", href = "/${tribe.id.value}/pin/new") {
                +"Add a new pin."
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

