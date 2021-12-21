package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.Paths.pinListPath
import com.zegreatrob.coupling.client.external.react.configInput
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.*
import react.router.Navigate
import react.useState

data class PinConfigEditoProps(
    val tribe: Tribe,
    val pin: Pin,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : DataProps

private val styles = useStyles("pin/PinConfigEditor")

fun RBuilder.pinConfigEditor(
    tribe: Tribe,
    pin: Pin,
    dispatchFunc: DispatchFunc<out PinCommandDispatcher>,
    reload: () -> Unit
) = child(PinConfigEditor, PinConfigEditoProps(tribe, pin, reload, dispatchFunc))

val PinConfigEditor = reactFunction { (tribe, pin, reload, dispatchFunc): PinConfigEditoProps ->
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc({ SavePinCommand(tribe.id, updatedPin) }) { reload() }
    val onRemove = pin.id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(tribe.id, pinId) }) { setRedirectUrl(tribe.id.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }

    if (redirectUrl != null)
        Navigate { attrs.to = redirectUrl }
    else
        span(styles.className) {
            configHeader(tribe) { +"Pin Configuration" }
            span(styles["pin"]) {
                pinConfigForm(updatedPin, onChange, onSubmit, onRemove)
//                promptOnExit(shouldShowPrompt = updatedPin != pin)
            }
            span(styles["icon"]) {
                pinButton(updatedPin, PinButtonScale.Large, showTooltip = false)
            }
        }
}

private fun RBuilder.pinConfigForm(
    pin: Pin,
    onChange: (Event) -> Unit,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?
) = child(ConfigForm) {
    attrs {
        this.onSubmit = onSubmit
        this.onRemove = onRemove
    }
    editorDiv(pin, onChange)
}

private fun RBuilder.editorDiv(pin: Pin, onChange: (Event) -> Unit) = div {
    editor {
        li { nameInput(pin, onChange) }
        li { iconInput(pin, onChange) }
        li { targetInput(onChange) }
    }
}

private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
    `when` = shouldShowPrompt,
    message = "You have unsaved data. Press OK to leave without saving."
)

private fun RBuilder.iconInput(pin: Pin, onChange: (Event) -> Unit) {
    configInput(
        labelText = "Icon",
        id = "pin-icon",
        name = "icon",
        value = pin.icon,
        type = InputType.text,
        placeholder = "Font-awesome icon codes, without the size class",
        onChange = onChange
    )
    span {
        +"This is the icon for the pin. This will be its primary identifier, so "
        a("https://fontawesome.com/icons?d=gallery&m=free") {
            +"choose wisely."
        }
    }
}

private fun RBuilder.nameInput(pin: Pin, onChange: (Event) -> Unit) {
    configInput(
        labelText = "Name",
        id = "pin-name",
        name = "name",
        value = pin.name,
        type = InputType.text,
        onChange = onChange,
        placeholder = "The name of the pin."
    )
    span { +"This is what you call the pin. You won't see this much." }
}

private fun RBuilder.targetInput(onChange: (Event) -> Unit) {
    label { attrs { htmlFor = "pinTarget" }; +"Target" }
    select {
        attrs {
            id = "pinTarget"
            name = "target"
            this["value"] = ""
            onChangeFunction = onChange
        }
        mapOf(
            PinTarget.Pair to "Pair"
        ).map { (rule, description) ->
            option {
                attrs {
                    key = "0"
                    value = rule.toValue()
                    label = description
                }
            }
        }
    }
    span { +"This is where the pin is assigned." }
}

@Suppress("unused")
private fun PinTarget.toValue(): String = ""
