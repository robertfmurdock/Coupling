package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import web.cssom.ClassName
import web.cssom.Clear
import web.cssom.Display
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.px
import web.dom.ElementId
import web.html.InputType
import web.html.text

external interface PinConfigContentProps : Props {
    var party: PartyDetails
    var boost: Boost?
    var pin: Pin
    var pinList: List<Pin>
    var onChange: (ChangeEvent<*>) -> Unit
    var onSubmit: () -> Unit
    var onRemove: (() -> Unit)?
}

val pinConfigContentClassName = ClassName("pin-config-content")

@ReactFunc
val PinConfigContent by nfc<PinConfigContentProps> { (party, boost, pin, pinList, onChange, onSubmit, onRemove) ->
    ConfigFrame {
        className = pinConfigContentClassName
        span {
            css { display = Display.inlineBlock }
            ConfigHeader(party = party, boost = boost) {
                +"Pin Configuration"
            }
            span {
                css {
                    position = Position.relative
                    clear = Clear.both
                    display = Display.inlineBlock
                    textAlign = TextAlign.center
                    textDecoration = None.none
                    verticalAlign = VerticalAlign.top
                    borderWidth = 11.px
                    color = NamedColor.black
                }
                pinConfigForm(pin, onChange, onSubmit, onRemove)
            }
            span {
                css {
                    marginLeft = 15.px
                    marginBottom = 15.px
                }
                PinButton(pin, PinButtonScale.Large, showTooltip = false)
            }
        }
        pinBag(party, pinList)
    }
}

private fun ChildrenBuilder.pinBag(party: PartyDetails, pinList: List<Pin>) = div {
    pinList.map { pin -> PinCard(party.id, pin, key = pin.id.value.toString()) }
}

private fun ChildrenBuilder.pinConfigForm(
    pin: Pin,
    onChange: (ChangeEvent<*>) -> Unit,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?,
) = ConfigForm(
    onSubmit = onSubmit,
    onRemove = onRemove,
) {
    editorDiv(pin, onChange)
}

private fun ChildrenBuilder.editorDiv(pin: Pin, onChange: (ChangeEvent<*>) -> Unit) = div {
    Editor {
        li { nameInput(pin, onChange) }
        li { iconInput(pin, onChange) }
        li { targetInput(onChange) }
    }
}

private fun ChildrenBuilder.iconInput(pin: Pin, onChange: (ChangeEvent<*>) -> Unit) {
    configInput(
        labelText = "Icon",
        id = "pin-icon",
        name = "icon",
        value = pin.icon,
        type = InputType.text,
        onChange = onChange,
        placeholder = "Font-awesome icon codes, without the size class",
        autoFocus = false,
    )
    span {
        +"This is the icon for the pin. This will be its primary identifier, so "
        a {
            href = "https://fontawesome.com/icons?d=gallery&m=free"
            +"choose wisely."
        }
    }
}

private fun ChildrenBuilder.nameInput(pin: Pin, onChange: (ChangeEvent<*>) -> Unit) {
    configInput(
        labelText = "Name",
        id = "pin-name",
        name = "name",
        value = pin.name,
        type = InputType.text,
        onChange = onChange,
        placeholder = "The name of the pin.",
        autoFocus = true,
    )
    span { +"This is what you call the pin. You won't see this much." }
}

private fun ChildrenBuilder.targetInput(onChange: (ChangeEvent<*>) -> Unit) {
    val elementId = ElementId("pinTarget")
    label {
        htmlFor = elementId
        +"Target"
    }
    select {
        id = elementId
        name = "target"
        this.value = ""
        this.onChange = onChange
        mapOf(
            PinTarget.Pair to "Pair",
        ).map { (_, description) ->
            option {
                key = "0"
                value = ""
                label = description
            }
        }
    }
    span { +"This is where the pin is assigned." }
}
