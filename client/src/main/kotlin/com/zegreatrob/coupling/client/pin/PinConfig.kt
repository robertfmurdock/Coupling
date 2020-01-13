package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
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
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
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
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

external interface PinConfigStyles {
    val className: String
    val saveButton: String
    val tribeBrowser: String
    val pinView: String
    val pinRoster: String
    val pin: String
    val deleteButton: String
}

typealias PinConfigContext = ScopedStyledRContext<PinConfigProps, PinConfigStyles>

interface PinConfigRenderer : ScopedStyledComponentRenderer<PinConfigProps, PinConfigStyles>,
    WindowFunctions, UseFormHook, SavePinCommandDispatcher, DeletePinCommandDispatcher {

    override val pinRepository: PinRepository

    override val componentPath: String get() = "pin/PinConfig"

    override fun ScopedStyledRContext<PinConfigProps, PinConfigStyles>.render() = with(props) {
        reactElement {
            div(classes = styles.className) {
                div {
                    div(classes = styles.tribeBrowser) {
                        tribeCard(TribeCardProps(tribe, pathSetter = pathSetter))
                    }
                    pinView(this)
                }
            }
        }
    }

    private fun PinConfigContext.pinView(rBuilder: RBuilder) {
        val (tribe, _, _, reload) = props
        val pin = props.pin

        val (values, onChange) = useForm(pin.toJson())
        val updatedPin = values.toPin()
        val onSubmitFunc = handleSubmitFunc { savePin(scope, updatedPin, tribe, reload) }

        val shouldShowPrompt = updatedPin != pin
        rBuilder.run {
            span(classes = styles.pinView) {
                span(classes = styles.pin) {
                    pinConfigForm(updatedPin, tribe, onChange, onSubmitFunc)()
                    prompt(
                        `when` = shouldShowPrompt,
                        message = "You have unsaved data. Would you like to save before you leave?"
                    )
                }
            }
        }
    }

    private fun handleSubmitFunc(handler: () -> Job) = { event: Event ->
        event.preventDefault()
        handler()
    }

    private fun savePin(
        scope: CoroutineScope,
        updatedPin: Pin,
        tribe: Tribe,
        reload: () -> Unit
    ) = scope.launch {
        SavePinCommand(tribe.id, updatedPin).perform()
        reload()
    }

    private fun removePin(
        tribe: Tribe,
        pathSetter: (String) -> Unit,
        scope: CoroutineScope,
        pinId: String
    ) = scope.launch {
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
                attrs { name = "pinForm"; onSubmitFunction = { event -> setIsSaving(true); onSubmit(event) } }

                div {
                    configInput(
                        labelText = "Name",
                        id = "pin-name",
                        name = "name",
                        value = pin.name ?: "",
                        type = InputType.text,
                        onChange = onChange
                    )
                }
                div {
                    configInput(
                        labelText = "Icon",
                        id = "pin-icon",
                        name = "icon",
                        value = pin.icon ?: "",
                        type = InputType.text,
                        onChange = onChange
                    )
                }

                button(classes = "large blue button") {
                    attrs {
                        classes += styles.saveButton
                        type = ButtonType.submit
                        tabIndex = "0"
                        value = "Save"
                        disabled = isSaving
                    }
                    +"Save"
                }
                val pinId = pin._id
                if (pinId != null) {
                    div(classes = "small red button") {
                        attrs {
                            classes += styles.deleteButton
                            onClickFunction = { removePin(tribe, props.pathSetter, scope, pinId) }
                        }
                        +"Retire"
                    }
                }
            }
        }
    }

}
