package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Editor
import com.zegreatrob.coupling.client.Paths.pinListPath
import com.zegreatrob.coupling.client.external.react.configInput
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
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
import react.router.Navigate
import react.useState

data class PinConfigEditor(
    val tribe: Tribe,
    val pin: Pin,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : DataProps<PinConfigEditor> {
    override val component: TMFC<PinConfigEditor> = pinConfigEditor
}

private val styles = useStyles("pin/PinConfigEditor")

val pinConfigEditor = tmFC { (tribe, pin, reload, dispatchFunc): PinConfigEditor ->
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc({ SavePinCommand(tribe.id, updatedPin) }) { reload() }
    val onRemove = pin.id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(tribe.id, pinId) }) { setRedirectUrl(tribe.id.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else
        span {
            className = styles.className
            ConfigHeader {
                this.tribe = tribe
                +"Pin Configuration"
            }
            span {
                className = styles["pin"]
                pinConfigForm(updatedPin, onChange, onSubmit, onRemove)
//                promptOnExit(shouldShowPrompt = updatedPin != pin)
            }
            span {
                className = styles["icon"]
                child(PinButton(updatedPin, PinButtonScale.Large, showTooltip = false))
            }
        }
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
        placeholder = "Font-awesome icon codes, without the size class",
        onChange = onChange
    )
    span {
        +"This is the icon for the pin. This will be its primary identifier, so "
        a {
            this.href = "https://fontawesome.com/icons?d=gallery&m=free"
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
        placeholder = "The name of the pin."
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
