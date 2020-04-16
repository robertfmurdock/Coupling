package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.CommandFunc
import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*
import kotlin.browser.window

data class PinConfigEditorProps(
    val tribe: Tribe,
    val pin: Pin,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val commandFunc: CommandFunc<PinCommandDispatcher>
) : RProps

private val styles = useStyles("pin/PinConfigEditor")

fun RBuilder.pinConfigEditor(
    tribe: Tribe,
    pin: Pin,
    commandFunc: CommandFunc<PinCommandDispatcher>,
    pathSetter: (String) -> Unit,
    reload: () -> Unit
) = child(
    PinConfigEditor.component.rFunction,
    PinConfigEditorProps(tribe, pin, pathSetter, reload, commandFunc)
)

val PinConfigEditor = reactFunction<PinConfigEditorProps> { (tribe, pin, pathSetter, reload, commandFunc) ->
    val (values, onChange) = useForm(pin.toJson())

    val updatedPin = values.toPin()

    val onSubmitFunc = commandFunc { SavePinCommand(tribe.id, updatedPin).perform(); reload() }
    val onRemoveFunc = { pinId: String -> commandFunc { removePin(tribe, pinId, pathSetter) } }

    span(classes = styles.className) {
        configHeader(tribe, pathSetter) { +"Pin Configuration" }
        span(classes = styles["pin"]) {
            pinConfigForm(updatedPin, onChange, onSubmitFunc, onRemoveFunc)
            promptOnExit(shouldShowPrompt = updatedPin != pin)
        }
        span(classes = styles["icon"]) {
            pinButton(updatedPin, PinButtonScale.Large, showTooltip = false)
        }
    }
}

private suspend fun PinCommandDispatcher.removePin(tribe: Tribe, pinId: String, pathSetter: (String) -> Unit) {
    if (window.confirm("Are you sure you want to delete this pin?")) {
        DeletePinCommand(tribe.id, pinId).perform()
        pathSetter("/${tribe.id.value}/pins")
    }
}

private inline fun RBuilder.pinConfigForm(
    pin: Pin,
    noinline onChange: (Event) -> Unit,
    crossinline onSubmit: () -> Unit,
    noinline onRemoveFunc: (String) -> () -> Unit
) = form {
    val (isSaving, setIsSaving) = useState(false)
    attrs {
        name = "pinForm"; onSubmitFunction =
        onSubmitFunction(setIsSaving, { onSubmit() })
    }

    div {
        editor {
            li { nameInput(pin, onChange) }
            li { iconInput(pin, onChange) }
            li { targetInput(onChange) }
        }
    }
    saveButton(isSaving, styles["saveButton"])
    val pinId = pin._id
    if (pinId != null) {
        retireButtonElement(onRemoveFunc(pinId))
    }
}

private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
    `when` = shouldShowPrompt,
    message = "You have unsaved data. Would you like to save before you leave?"
)




private fun RBuilder.retireButtonElement(onRetire: () -> Unit) = div(classes = "small red button") {
    attrs {
        classes += styles["deleteButton"]
        onClickFunction = { onRetire() }
    }
    +"Retire"
}

private fun onSubmitFunction(setIsSaving: (Boolean) -> Unit, onSubmit: (Event) -> Unit): (Event) -> Unit =
    { event -> setIsSaving(true); onSubmit(event) }

private fun RBuilder.saveButton(isSaving: Boolean, className: String) = button(
    classes = "super blue button"
) {
    attrs {
        classes += className
        type = ButtonType.submit
        tabIndex = "0"
        value = "Save"
        disabled = isSaving
    }
    +"Save"
}

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
