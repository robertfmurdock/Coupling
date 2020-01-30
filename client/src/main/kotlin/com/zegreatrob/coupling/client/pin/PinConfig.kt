package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.Editor.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactrouter.prompt
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.client.pin.PinCard.pinCard
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
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
import react.ReactElement
import react.dom.*

object PinConfig : RComponent<PinConfigProps>(provider()), PinConfigRenderer, RepositoryCatalog by SdkSingleton

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
    WindowFunctions, SavePinCommandDispatcher, DeletePinCommandDispatcher {

    override val pinRepository: PinRepository
    override val componentPath: String get() = "pin/PinConfig"

    override fun PinConfigContext.render() = reactElement {
        val (tribe, _, pinList, _) = props
        configFrame(styles.className) {
            pinConfigEditor(props, scope, styles)
            pinBag(tribe, pinList, styles.pinBag)
        }
    }

    private inline fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
        pinList.map { pin -> pinCard(tribe.id, pin, key = pin._id) }
    }

    private inline fun RBuilder.pinConfigEditor(
        props: PinConfigProps,
        scope: CoroutineScope,
        styles: PinConfigStyles
    ): ReactElement {
        val (tribe, _, _, pathSetter, reload) = props

        val (values, onChange) = useForm(props.pin.toJson())
        val updatedPin = values.toPin()
        val onSubmitFunc = handleSubmitFunc { savePin(updatedPin, tribe, reload) }

        val shouldShowPrompt = updatedPin != props.pin
        return span(classes = styles.pinView) {
            configHeader(tribe, pathSetter) { +"Pin Configuration" }
            span(classes = styles.pin) {
                pinConfigForm(updatedPin, tribe, pathSetter, onChange, onSubmitFunc, scope, styles)
                prompt(
                    `when` = shouldShowPrompt,
                    message = "You have unsaved data. Would you like to save before you leave?"
                )
            }
            span(classes = styles.icon) {
                pinButton(updatedPin, PinButtonScale.Large, key = null, showTooltip = false)
            }
        }
    }

    private inline fun handleSubmitFunc(crossinline handler: CoroutineScope.() -> Job) =
        { scope: CoroutineScope, event: Event ->
            event.preventDefault()
            scope.handler()
        }

    private inline fun CoroutineScope.savePin(updatedPin: Pin, tribe: Tribe, crossinline reload: () -> Unit) = launch {
        SavePinCommand(tribe.id, updatedPin).perform()
        reload()
    }

    private fun CoroutineScope.removePin(tribe: Tribe, pinId: String, pathSetter: (String) -> Unit) = launch {
        if (window.confirm("Are you sure you want to delete this pin?")) {
            DeletePinCommand(tribe.id, pinId).perform()
            pathSetter("/${tribe.id.value}/pins")
        }
    }

    private inline fun RBuilder.pinConfigForm(
        pin: Pin,
        tribe: Tribe,
        noinline pathSetter: (String) -> Unit,
        noinline onChange: (Event) -> Unit,
        crossinline onSubmit: (CoroutineScope, Event) -> Job,
        scope: CoroutineScope,
        styles: PinConfigStyles
    ) = form {
        val (isSaving, setIsSaving) = useState(false)
        attrs {
            name = "pinForm"; onSubmitFunction =
            onSubmitFunction(setIsSaving, { event -> onSubmit(scope, event) })
        }

        div {
            editor {
                li { nameInput(pin, onChange) }
                li { iconInput(pin, onChange) }
                li { targetInput(onChange) }
            }
        }
        saveButton(isSaving, styles.saveButton)
        val pinId = pin._id
        if (pinId != null) {
            retireButtonElement(tribe, pinId, pathSetter, scope, styles)
        }
    }

    private inline fun RBuilder.retireButtonElement(
        tribe: Tribe,
        pinId: String,
        noinline pathSetter: (String) -> Unit,
        scope: CoroutineScope,
        styles: PinConfigStyles
    ) = div(classes = "small red button") {
        attrs {
            classes += styles.deleteButton
            onClickFunction = { scope.removePin(tribe, pinId, pathSetter) }
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
            value = pin.icon ?: "",
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
            value = pin.name ?: "",
            type = InputType.text,
            onChange = onChange,
            placeholder = "The name of the pin."
        )
        span { +"This is what you call the pin. You won't see this much." }
    }

    private inline fun RBuilder.targetInput(noinline onChange: (Event) -> Unit) {
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

