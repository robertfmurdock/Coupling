package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.configInput
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.ChildrenBuilder
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
import web.html.InputType

data class PinConfigContent(
    val party: PartyDetails,
    val pin: Pin,
    val pinList: List<Pin>,
    val onChange: (ChangeEvent<*>) -> Unit,
    val onSubmit: () -> Unit,
    val onRemove: (() -> Unit)?,
) : DataPropsBind<PinConfigContent>(pinConfigContent)

val pinConfigContentClassName = ClassName("pin-config-content")

val pinConfigContent by ntmFC<PinConfigContent> { (party, pin, pinList, onChange, onSubmit, onRemove) ->
    ConfigFrame {
        className = pinConfigContentClassName
        span {
            css { display = Display.inlineBlock }
            ConfigHeader {
                this.party = party
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
    pinList.map { pin -> PinCard(party.id, pin, key = pin.id) }
}

private fun ChildrenBuilder.pinConfigForm(
    pin: Pin,
    onChange: (ChangeEvent<*>) -> Unit,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?,
) = ConfigForm {
    this.onSubmit = onSubmit
    this.onRemove = onRemove
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
    label { htmlFor = "pinTarget"; +"Target" }
    select {
        id = "pinTarget"
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
