package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configForm
import com.zegreatrob.coupling.client.configHeader
import com.zegreatrob.coupling.client.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*

data class PinConfigEditorProps(
    val tribe: Tribe,
    val pin: Pin,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : RProps

private val styles = useStyles("pin/PinConfigEditor")

fun RBuilder.pinConfigEditor(
    tribe: Tribe,
    pin: Pin,
    dispatchFunc: DispatchFunc<out PinCommandDispatcher>,
    pathSetter: (String) -> Unit,
    reload: () -> Unit
) = child(
    PinConfigEditor,
    PinConfigEditorProps(tribe, pin, pathSetter, reload, dispatchFunc)
)

val PinConfigEditor = reactFunction<PinConfigEditorProps> { (tribe, pin, pathSetter, reload, dispatchFunc) ->
    val (values, onChange) = useForm(pin.toJson())

    val updatedPin = values.toPin()

    val onSubmit = dispatchFunc({ SavePinCommand(tribe.id, updatedPin) }) { reload() }
    val onRemove = pin._id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(tribe.id, pinId) }) { pathSetter(tribe.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }

    span(styles.className) {
        configHeader(tribe, pathSetter) { +"Pin Configuration" }
        span(styles["pin"]) {
            pinConfigForm(updatedPin, onChange, onSubmit, onRemove)
            promptOnExit(shouldShowPrompt = updatedPin != pin)
        }
        span(styles["icon"]) {
            pinButton(updatedPin, PinButtonScale.Large, showTooltip = false)
        }
    }
}

private fun Tribe.pinListPath() = "/${id.value}/pins"

private fun RBuilder.pinConfigForm(
    pin: Pin,
    onChange: (Event) -> Unit,
    onSubmit: () -> Unit,
    onRemove: (() -> Unit)?
) = configForm("pinForm", onSubmit, onRemove) {
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
    message = "You have unsaved data. Would you like to save before you leave?"
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
