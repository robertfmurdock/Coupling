package com.zegreatrob.coupling.client.tribe

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.*

data class TribeConfigProps(
    val tribe: Tribe,
    val pathSetter: (String) -> Unit,
    val dispatchFunc: DispatchFunc<out TribeConfigDispatcher>
) : RProps

interface TribeConfigDispatcher : SaveTribeCommandDispatcher, DeleteTribeCommandDispatcher {
    override val tribeRepository: TribeRepository
}

private val styles = useStyles("tribe/TribeConfig")

val TribeConfig = reactFunction<TribeConfigProps> { (tribe, pathSetter, commandFunc) ->
    val isNew = tribe.id.value == ""

    val (values, onChange) = useForm(tribe.withDefaultTribeId().toJson())
    val updatedTribe = values.toTribe()

    val onSave = commandFunc({ SaveTribeCommand(updatedTribe) }) { pathSetter.tribeList() }
    val onDelete = if (isNew) null else commandFunc({ DeleteTribeCommand(tribe.id) }) { pathSetter.tribeList() }

    configFrame(styles.className) {
        configHeader(tribe, pathSetter) { +"Tribe Configuration" }
        div {
            tribeConfigEditor(updatedTribe, isNew, onChange, onSave, onDelete)
            tribeCard(TribeCardProps(updatedTribe, pathSetter = pathSetter))
        }
    }
}

private fun Tribe.withDefaultTribeId() = if (id.value.isNotBlank())
    this
else
    copy(id = TribeId("${uuid4()}"))

private fun RBuilder.tribeConfigEditor(
    updatedTribe: Tribe,
    isNew: Boolean,
    onChange: (Event) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?
) = span(styles["tribeConfigEditor"]) {
    configForm(onSubmit = onSave, onRemove = onDelete) {
        editorDiv(updatedTribe, onChange, isNew)
    }
}

private fun RBuilder.editorDiv(
    tribe: Tribe,
    onChange: (Event) -> Unit,
    isNew: Boolean
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
                li {
                    uniqueIdInput(tribe, onChange)
                    span { +"This affects your tribe's URL. This is permanently assigned." }
                }
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

private fun RBuilder.altBadgeInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Alt Badge Name",
    id = "alt-badge-name",
    name = "alternateBadgeName",
    value = tribe.alternateBadgeName,
    type = InputType.text,
    onChange = onChange
)

private fun RBuilder.defaultBadgeInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Default Badge Name",
    id = "default-badge-name",
    name = "defaultBadgeName",
    value = tribe.defaultBadgeName,
    type = InputType.text,
    onChange = onChange
)

private fun RBuilder.enableBadgesInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Enable Badges",
    id = "badge-checkbox",
    name = "badgesEnabled",
    value = tribe.id.value,
    type = InputType.checkBox,
    onChange = onChange,
    checked = tribe.badgesEnabled
)

private fun RBuilder.enableAnimationsInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Enable Animations",
    id = "animations-checkbox",
    name = "animationsEnabled",
    value = tribe.id.value,
    type = InputType.checkBox,
    onChange = onChange,
    checked = tribe.animationEnabled
)

private fun RBuilder.enableCallSignsInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Enable Call Signs",
    id = "call-sign-checkbox",
    name = "callSignsEnabled",
    value = tribe.id.value,
    type = InputType.checkBox,
    onChange = onChange,
    checked = tribe.callSignsEnabled
)

private fun RBuilder.uniqueIdInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Unique Id",
    id = "tribe-id",
    name = "id",
    value = tribe.id.value,
    type = InputType.text,
    onChange = onChange
)

private fun RBuilder.emailInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Email",
    id = "tribe-email",
    name = "email",
    value = tribe.email ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the tribe email here"
)

private fun RBuilder.nameInput(tribe: Tribe, onChange: (Event) -> Unit) = configInput(
    labelText = "Name",
    id = "tribe-name",
    name = "name",
    value = tribe.name ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the tribe name here"
)

private val pairingRuleDescriptions = mapOf(
    PairingRule.LongestTime to "Prefer Longest Time",
    PairingRule.PreferDifferentBadge to "Prefer Different Badges (Beta)"
)
