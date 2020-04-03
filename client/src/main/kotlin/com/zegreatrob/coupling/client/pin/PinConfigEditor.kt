package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.ScopeProvider
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.Editor.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*

data class PinConfigEditorProps(
    val tribe: Tribe,
    val pin: Pin,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val commandDispatcher: PinCommandDispatcher
) : RProps

open class PinConfigEditor(provider: ScopeProvider) : FRComponent<PinConfigEditorProps>(provider()),
    ScopeProvider by provider, ReactScopeProvider, WindowFunctions {

    companion object : PinConfigEditor(object : ReactScopeProvider {}) {

        fun RBuilder.pinConfigEditor(
            tribe: Tribe,
            pin: Pin,
            dispatcher: CommandDispatcher,
            pathSetter: (String) -> Unit,
            reload: () -> Unit
        ) = child(PinConfigEditor.component.rFunction, PinConfigEditorProps(tribe, pin, pathSetter, reload, dispatcher))

    }

    val styles = useStyles("pin/PinConfigEditor")

    override fun render(props: PinConfigEditorProps) = reactElement {
        val (tribe, _, pathSetter, reload, dispatcher) = props
        val scope = useScope(styles.className)
        val (values, onChange) = useForm(props.pin.toJson())

        val updatedPin = values.toPin()
        val onSubmitFunc = handleSubmitFunc { dispatcher.savePin(scope, updatedPin, tribe, reload) }
        val onRemove = onRemove(dispatcher, scope, tribe, pathSetter)

        span(classes = styles.className) {
            configHeader(tribe, pathSetter) { +"Pin Configuration" }
            span(classes = styles["pin"]) {
                pinConfigForm(updatedPin, onChange, onSubmitFunc, onRemove)
                promptOnExit(shouldShowPrompt = updatedPin != props.pin)
            }
            span(classes = styles["icon"]) {
                pinButton(updatedPin, PinButtonScale.Large, showTooltip = false)
            }
        }
    }

    private fun onRemove(
        dispatcher: PinCommandDispatcher,
        scope: CoroutineScope,
        tribe: Tribe,
        pathSetter: (String) -> Unit
    ): (String) -> Unit = { pinId: String -> dispatcher.removePin(scope, tribe, pinId, pathSetter) }

    private fun RBuilder.promptOnExit(shouldShowPrompt: Boolean) = prompt(
        `when` = shouldShowPrompt,
        message = "You have unsaved data. Would you like to save before you leave?"
    )

    private inline fun handleSubmitFunc(crossinline handler: () -> Job) = { event: Event ->
        event.preventDefault()
        handler()
    }

    private inline fun RBuilder.pinConfigForm(
        pin: Pin,
        noinline onChange: (Event) -> Unit,
        crossinline onSubmit: (Event) -> Job,
        noinline onRemove: (String) -> Unit
    ) = form {
        val (isSaving, setIsSaving) = useState(false)
        attrs {
            name = "pinForm"; onSubmitFunction =
            onSubmitFunction(setIsSaving, { event -> onSubmit(event) })
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
            retireButtonElement { onRemove(pinId) }
        }
    }

    private inline fun PinCommandDispatcher.savePin(
        scope: CoroutineScope,
        updatedPin: Pin,
        tribe: Tribe,
        crossinline reload: () -> Unit
    ) = scope.launch {
        SavePinCommand(tribe.id, updatedPin).perform()
        reload()
    }

    private fun PinCommandDispatcher.removePin(
        scope: CoroutineScope,
        tribe: Tribe,
        pinId: String,
        pathSetter: (String) -> Unit
    ) = scope.launch {
        if (window.confirm("Are you sure you want to delete this pin?")) {
            DeletePinCommand(tribe.id, pinId).perform()
            pathSetter("/${tribe.id.value}/pins")
        }
    }

    private fun RBuilder.retireButtonElement(onRetire: (Event) -> Unit) = div(classes = "small red button") {
        attrs {
            classes += styles["deleteButton"]
            onClickFunction = onRetire
        }
        +"Retire"
    }

    private fun onSubmitFunction(setIsSaving: (Boolean) -> Unit, onSubmit: (Event) -> Job): (Event) -> Unit =
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

}

@Suppress("unused")
private fun PinTarget.toValue(): String = ""
