package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.Editor.editor
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.tabIndex
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.*

object TribeConfig : RComponent<TribeConfigProps>(provider()), TribeConfigBuilder,
    RepositoryCatalog by SdkSingleton

data class TribeConfigProps(val tribe: Tribe, val pathSetter: (String) -> Unit) : RProps

external interface TribeConfigStyles {
    val className: String
    val saveButton: String
    val tribeConfigEditor: String
}

typealias TribeConfigRenderer = ScopedStyledRContext<TribeConfigProps, TribeConfigStyles>

interface TribeConfigBuilder : ScopedStyledComponentRenderer<TribeConfigProps, TribeConfigStyles>,
    SaveTribeCommandDispatcher, DeleteTribeCommandDispatcher, NullTraceIdProvider {
    override val tribeRepository: TribeRepository

    override val componentPath: String get() = "tribe/TribeConfig"

    override fun ScopedStyledRContext<TribeConfigProps, TribeConfigStyles>.render(): ReactElement {
        val isNew = props.tribe.id.value == ""

        val (values, onChange) = useForm(props.tribe.toJson())
        val updatedTribe = values.toTribe()

        val onSave = { onClickSave(updatedTribe) }
        val onDelete = { onClickDelete() }

        return reactElement {
            configFrame(styles.className) {
                configHeader(props.tribe, props.pathSetter) { +"Tribe Configuration" }
                div {
                    span(styles.tribeConfigEditor) {
                        configInputs(
                            tribe = updatedTribe,
                            isNew = isNew,
                            onChange = onChange,
                            onSave = onSave,
                            onDelete = onDelete,
                            styles = styles
                        )
                    }
                    tribeCard(TribeCardProps(updatedTribe, pathSetter = props.pathSetter))
                }
            }
        }
    }

    private inline fun RBuilder.retireButton(crossinline onDelete: () -> Job) =
        div(classes = "small red button delete-tribe-button") {
            attrs { onClickFunction = { onDelete() } }
            +"Retire"
        }

    private inline fun RBuilder.saveButton(styles: TribeConfigStyles, crossinline onSave: () -> Job) =
        input(InputType.button, classes = "super blue button") {
            attrs {
                classes += styles.saveButton
                tabIndex = "0"
                value = "Save"
                onClickFunction = { onSave() }
            }
        }

    private fun TribeConfigRenderer.onClickDelete() = scope.launch {
        DeleteTribeCommand(props.tribe.id).perform()
        props.pathSetter("/tribes/")
    }

    private fun TribeConfigRenderer.onClickSave(updatedTribe: Tribe) = scope.launch {
        SaveTribeCommand(updatedTribe).perform()
        props.pathSetter("/tribes/")
    }

    private fun RBuilder.configInputs(
        tribe: Tribe,
        isNew: Boolean,
        onChange: (Event) -> Unit,
        onSave: () -> Job,
        onDelete: () -> Job,
        styles: TribeConfigStyles
    ) {
        div {
            editor {
                li {
                    nameInput(tribe, onChange)
                    span { +"The full tribe name!" }
                }
                li {
                    emailInput(tribe, onChange)
                    span { +"The tribe email address - Attach a Gravatar to this to cheese your tribe icon." }
                }

                if (isNew) {
                    li { uniqueIdInput(tribe, onChange) }
                }
                li {
                    enableAnimationsInput(tribe, onChange)
                    span { +"Keep things wacky and springy, or still and deadly serious." }
                }
                li {
                    animationSpeedSelect(tribe, onChange)
                    span { +"In case you want things to move a little... faster." }
                }
                li {
                    enableCallSignsInput(tribe, onChange)
                    span { +"Every Couple needs a Call Sign. Makes things more fun!" }
                }
                li {
                    enableBadgesInput(tribe, onChange)
                    span { +"Advanced users only: this lets you divide your tribe into two groups." }
                }
                li {
                    defaultBadgeInput(tribe, onChange)
                    span { +"The first badge a player can be given. When badges are enabled, existing players default to having this badge." }
                }
                li {
                    altBadgeInput(tribe, onChange)
                    span { +"The other badge a player can be given. A player can only have one badge at a time." }
                }
                li {
                    pairingRuleSelect(tribe, onChange)
                    span { +"Advanced users only: This rule affects how players are assigned." }
                }
            }
        }
        saveButton(styles, onSave)
        if (!isNew) {
            retireButton(onDelete)
        }
    }

    private fun RBuilder.animationSpeedSelect(tribe: Tribe, onChange: (Event) -> Unit) {
        label {
            attrs { htmlFor = "animation-speed" }
            +"Animation Speed"
        }
        select {
            attrs {
                id = "animation-speed"
                name = "animationSpeed"
                this["value"] = "${tribe.animationSpeed}"
                onChangeFunction = onChange
            }
            listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2, 3, 4)
                .map { speed ->
                    option {
                        attrs {
                            key = "$speed"
                            value = "$speed"
                            label = "${speed}x"
                        }
                    }
                }
        }
    }

    private fun RBuilder.pairingRuleSelect(tribe: Tribe, onChange: (Event) -> Unit) {
        label {
            attrs { htmlFor = "pairing-rule" }
            +"Pairing Rule"
        }
        select {
            attrs {
                id = "pairing-rule"
                name = "pairingRule"
                this["value"] = "${toValue(tribe.pairingRule)}"
                onChangeFunction = onChange
            }
            pairingRuleDescriptions
                .map { (rule, description) ->
                    option {
                        attrs {
                            key = "${toValue(rule)}"
                            value = "${toValue(rule)}"
                            label = description
                        }
                    }
                }
        }
    }

    private fun RBuilder.altBadgeInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Alt Badge Name",
            id = "alt-badge-name",
            name = "alternateBadgeName",
            value = tribe.alternateBadgeName ?: "",
            type = InputType.text,
            onChange = onChange
        )
    }

    private fun RBuilder.defaultBadgeInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Default Badge Name",
            id = "default-badge-name",
            name = "defaultBadgeName",
            value = tribe.defaultBadgeName ?: "",
            type = InputType.text,
            onChange = onChange
        )
    }

    private fun RBuilder.enableBadgesInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Enable Badges",
            id = "badge-checkbox",
            name = "badgesEnabled",
            value = tribe.id.value,
            type = InputType.checkBox,
            onChange = onChange,
            checked = tribe.badgesEnabled
        )
    }

    private fun RBuilder.enableAnimationsInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Enable Animations",
            id = "animations-checkbox",
            name = "animationsEnabled",
            value = tribe.id.value,
            type = InputType.checkBox,
            onChange = onChange,
            checked = tribe.animationEnabled
        )
    }

    private fun RBuilder.enableCallSignsInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Enable Call Signs",
            id = "call-sign-checkbox",
            name = "callSignsEnabled",
            value = tribe.id.value,
            type = InputType.checkBox,
            onChange = onChange,
            checked = tribe.callSignsEnabled
        )
    }

    private fun RBuilder.uniqueIdInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Unique Id",
            id = "tribe-id",
            name = "id",
            value = tribe.id.value,
            type = InputType.text,
            onChange = onChange
        )
    }

    private fun RBuilder.emailInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Email",
            id = "tribe-email",
            name = "email",
            value = tribe.email ?: "",
            type = InputType.text,
            onChange = onChange,
            placeholder = "Enter the tribe email here"
        )
    }

    private fun RBuilder.nameInput(
        tribe: Tribe,
        onChange: (Event) -> Unit
    ) {
        configInput(
            labelText = "Name",
            id = "tribe-name",
            name = "name",
            value = tribe.name ?: "",
            type = InputType.text,
            onChange = onChange,
            placeholder = "Enter the tribe name here"
        )
    }
}

private val pairingRuleDescriptions = mapOf(
    PairingRule.LongestTime to "Prefer Longest Time",
    PairingRule.PreferDifferentBadge to "Prefer Different Badges (Beta)"
)
