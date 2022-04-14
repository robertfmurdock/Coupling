package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.react.configInput
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.display
import org.w3c.dom.HTMLSelectElement
import react.ChildrenBuilder
import react.dom.events.ChangeEvent
import react.dom.events.ChangeEventHandler
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
import react.key

data class PartyConfigContent(
    var tribe: Party,
    var isNew: Boolean?,
    var onChange: (ChangeEvent<*>) -> Unit,
    var onSave: () -> Unit,
    var onDelete: (() -> Unit)?,
) : DataPropsBind<PartyConfigContent>(partyConfigContent)

private val styles = useStyles("party/TribeConfig")

val partyConfigContent = tmFC<PartyConfigContent> { (tribe, isNew, onChange, onSave, onDelete) ->
    ConfigFrame {
        className = styles.className
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")

        ConfigHeader {
            this.party = tribe
            +"Party Configuration"
        }
        cssDiv(css = { display = Display.flex }) {
            tribeConfigEditor(tribe, isNew ?: false, onChange, onSave, onDelete)
            child(PartyCard(tribe))
        }
    }
}

private fun ChildrenBuilder.tribeConfigEditor(
    updatedTribe: Party,
    isNew: Boolean,
    onChange: (ChangeEvent<*>) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?
) = span {
    className = styles["tribeConfigEditor"]
    ConfigForm {
        this.onSubmit = onSave
        this.onRemove = onDelete
        editorDiv(updatedTribe, onChange, isNew)
    }
}

private fun ChildrenBuilder.editorDiv(tribe: Party, onChange: (ChangeEvent<*>) -> Unit, isNew: Boolean) =
    div {
        Editor {
            li {
                nameInput(tribe, onChange)
                span { +"The full tribe name!" }
            }
            li {
                emailInput(tribe, onChange)
                span {
                    +"The tribe email address - Attach a"
                    gravatarLink {}
                    +"to this to cheese your tribe icon."
                }
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

private fun ChildrenBuilder.animationSpeedSelect(tribe: Party, onChange: ChangeEventHandler<HTMLSelectElement>) {
    label {
        htmlFor = "animation-speed"
        +"Animation Speed"
    }
    select {
        id = "animation-speed"
        name = "animationSpeed"
        this.value = "${tribe.animationSpeed}"
        this.onChange = onChange
        listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2, 3, 4)
            .map { speed ->
                option {
                    key = "$speed"
                    value = "$speed"
                    label = "${speed}x"
                }
            }
    }
}

private fun ChildrenBuilder.pairingRuleSelect(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) {
    label {
        htmlFor = "pairing-rule"
        +"Pairing Rule"
    }
    select {
        id = "pairing-rule"
        name = "pairingRule"
        this.value = "${PairingRule.toValue(tribe.pairingRule)}"
        this.onChange = { event -> onChange(event) }
        pairingRuleDescriptions
            .map { (rule, description) ->
                option {
                    key = "${PairingRule.toValue(rule)}"
                    value = "${PairingRule.toValue(rule)}"
                    label = description
                }
            }
    }
}

private fun ChildrenBuilder.altBadgeInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Alt Badge Name",
    id = "alt-badge-name",
    name = "alternateBadgeName",
    value = tribe.alternateBadgeName,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.defaultBadgeInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Default Badge Name",
    id = "default-badge-name",
    name = "defaultBadgeName",
    value = tribe.defaultBadgeName,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.enableBadgesInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Badges",
    id = "badge-checkbox",
    name = "badgesEnabled",
    value = tribe.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = tribe.badgesEnabled,
)

private fun ChildrenBuilder.enableAnimationsInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Animations",
    id = "animations-checkbox",
    name = "animationsEnabled",
    value = tribe.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = tribe.animationEnabled,
)

private fun ChildrenBuilder.enableCallSignsInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Call Signs",
    id = "call-sign-checkbox",
    name = "callSignsEnabled",
    value = tribe.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = tribe.callSignsEnabled,
)

private fun ChildrenBuilder.uniqueIdInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Unique Id",
    id = "tribe-id",
    name = "id",
    value = tribe.id.value,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.emailInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Email",
    id = "tribe-email",
    name = "email",
    value = tribe.email ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the tribe email here",
)

private fun ChildrenBuilder.nameInput(tribe: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Name",
    id = "tribe-name",
    name = "name",
    value = tribe.name ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the tribe name here",
    autoFocus = true
)

private val pairingRuleDescriptions = mapOf(
    PairingRule.LongestTime to "Prefer Longest Time",
    PairingRule.PreferDifferentBadge to "Prefer Different Badges (Beta)"
)
