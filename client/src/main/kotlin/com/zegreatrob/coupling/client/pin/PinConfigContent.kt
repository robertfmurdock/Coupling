package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.ConfigFrame
import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.Editor
import com.zegreatrob.coupling.client.external.react.configInput
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import react.ChildrenBuilder
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.key

private val styles = useStyles("pin/PinConfigEditor")

data class PinConfigContent(
    val party: Party,
    val pin: Pin,
    val pinList: List<Pin>,
    val onChange: (ChangeEvent<*>) -> Unit,
    val onSubmit: () -> Unit,
    val onRemove: (() -> Unit)?
) : DataPropsBind<PinConfigContent>(pinConfigContent)

private val pinConfigStyles = useStyles("pin/PinConfig")

val pinConfigContent = tmFC<PinConfigContent> { (party, pin, pinList, onChange, onSubmit, onRemove) ->
    ConfigFrame {
        className = pinConfigStyles.className
        span {
            className = styles.className
            ConfigHeader {
                this.party = party
                +"Pin Configuration"
            }
            span {
                className = styles["pin"]
                pinConfigForm(pin, onChange, onSubmit, onRemove)
//                promptOnExit(shouldShowPrompt = updatedPin != pin)
            }
            span {
                className = styles["icon"]
                child(PinButton(pin, PinButtonScale.Large, showTooltip = false))
            }
        }
        pinBag(party, pinList, pinConfigStyles["pinBag"])
    }
}

private fun ChildrenBuilder.pinBag(party: Party, pinList: List<Pin>, className: ClassName) = div {
    this.className = className
    pinList.map { pin -> child(PinCard(party.id, pin), key = pin.id) }
}

private fun ChildrenBuilder.pinConfigForm(
    pin: Pin,
    onChange: (ChangeEvent<*>) -> Unit,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?
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

private fun ChildrenBuilder.promptOnExit(shouldShowPrompt: Boolean) = PromptComponent {
    `when` = shouldShowPrompt
    message = "You have unsaved data. Press OK to leave without saving."
}

private fun ChildrenBuilder.iconInput(pin: Pin, onChange: (ChangeEvent<*>) -> Unit) {
    configInput(
        labelText = "Icon",
        id = "pin-icon",
        name = "icon",
        value = pin.icon,
        type = react.dom.html.InputType.text,
        onChange = onChange,
        placeholder = "Font-awesome icon codes, without the size class",
        autoFocus = false
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
        type = react.dom.html.InputType.text,
        onChange = onChange,
        placeholder = "The name of the pin.",
        autoFocus = true
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
            PinTarget.Pair to "Pair"
        ).map { (rule, description) ->
            option {
                key = "0"
                value = rule.toValue()
                label = description
            }
        }
    }
    span { +"This is where the pin is assigned." }
}

@Suppress("unused")
private fun PinTarget.toValue(): String = ""
