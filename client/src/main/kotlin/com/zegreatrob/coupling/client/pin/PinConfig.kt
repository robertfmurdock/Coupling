package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.Editor.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.tabIndex
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

object PinConfig : RComponent<PinConfigProps>(provider()), PinConfigRenderer,
    RepositoryCatalog by SdkSingleton

data class PinConfigProps(
    val tribe: Tribe,
    val pin: Pin,
    val pinList: List<Pin>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

external interface PinConfigStyles {
    val className: String
    val icon: String
    val saveButton: String
    val tribeBrowser: String
    val pinView: String
    val pin: String
    val deleteButton: String
    val pinBag: String
}

typealias PinConfigContext = ScopedStyledRContext<PinConfigProps, PinConfigStyles>

interface PinConfigRenderer : ScopedStyledComponentRenderer<PinConfigProps, PinConfigStyles>,
    WindowFunctions, UseFormHook, SavePinCommandDispatcher, DeletePinCommandDispatcher {

    override val pinRepository: PinRepository
    override val componentPath: String get() = "pin/PinConfig"

    override fun PinConfigContext.render() = reactElement {
        val (tribe, _, _, pathSetter) = props
        div(classes = styles.className) {
            div {
                div(classes = styles.tribeBrowser) {
                    tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                }
                child(pinViewElement())
                child(pinBagElement())
            }
        }
    }

    fun PinConfigContext.pinBagElement(): ReactElement {
        val tribeId = props.tribe.id
        return reactElement {
            div(styles.pinBag) {
                props.pinList.map { pin ->
                    child(PinCard(PinCardProps(tribeId, pin), key = pin._id))
                }
            }
        }
    }

    private fun PinConfigContext.pinViewElement(): ReactElement {
        val (tribe, _, _, _, reload) = props

        val (values, onChange) = useForm(props.pin.toJson())
        val updatedPin = values.toPin()
        val onSubmitFunc = handleSubmitFunc { savePin(scope, updatedPin, tribe, reload) }

        val shouldShowPrompt = updatedPin != props.pin
        return reactElement {
            span(classes = styles.pinView) {
                div {
                    h1 { +"Pin Configuration" }
                }

                span(classes = styles.pin) {
                    pinConfigForm(updatedPin, tribe, onChange, onSubmitFunc)()
                    prompt(
                        `when` = shouldShowPrompt,
                        message = "You have unsaved data. Would you like to save before you leave?"
                    )
                }
                span(classes = styles.icon) {
                    pinButton(updatedPin, PinButtonScale.Large)
                }
            }
        }
    }

    private fun handleSubmitFunc(handler: () -> Job) = { event: Event ->
        event.preventDefault()
        handler()
    }

    private fun savePin(scope: CoroutineScope, updatedPin: Pin, tribe: Tribe, reload: () -> Unit) = scope.launch {
        SavePinCommand(tribe.id, updatedPin).perform()
        reload()
    }

    private fun removePin(tribe: Tribe, pathSetter: (String) -> Unit, scope: CoroutineScope, pinId: String) =
        scope.launch {
            if (window.confirm("Are you sure you want to delete this pin?")) {
                DeletePinCommand(tribe.id, pinId).perform()
                pathSetter("/${tribe.id.value}/pairAssignments/current/")
            }
        }

    private fun PinConfigContext.pinConfigForm(
        pin: Pin,
        tribe: Tribe,
        onChange: (Event) -> Unit,
        onSubmit: (Event) -> Job
    ): RBuilder.() -> ReactElement {
        val (isSaving, setIsSaving) = useState(false)
        return {
            form {
                attrs { name = "pinForm"; onSubmitFunction = onSubmitFunction(setIsSaving, onSubmit) }

                div {
                    editor {
                        li { nameInput(pin, onChange) }
                        li { iconInput(pin, onChange) }
                    }
                }
                saveButton(isSaving, styles.saveButton)
                val pinId = pin._id
                if (pinId != null) {
                    child(retireButtonElement(tribe, pinId))
                }
            }
        }
    }

    private fun PinConfigContext.retireButtonElement(tribe: Tribe, pinId: String) = reactElement {
        div(classes = "small red button") {
            attrs {
                classes += styles.deleteButton
                onClickFunction =
                    {
                        removePin(
                            tribe,
                            props.pathSetter,
                            scope,
                            pinId
                        )
                    }
            }
            +"Retire"
        }
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
            value = pin.icon ?: "",
            type = InputType.text,
            placeholder = "Font-awesome icon codes, without the size class",
            onChange = onChange
        )
        span { +"This is the icon for the pin. This will be its primary identifier, so choose wisely." }
    }

    private fun RBuilder.nameInput(pin: Pin, onChange: (Event) -> Unit) {
        configInput(
            labelText = "Name",
            id = "pin-name",
            name = "name",
            value = pin.name ?: "",
            type = InputType.text,
            onChange = onChange,
            placeholder = "The name of the pin."
        )
        span { +"This is what you call the pin. You won't see this much." }
    }

}

