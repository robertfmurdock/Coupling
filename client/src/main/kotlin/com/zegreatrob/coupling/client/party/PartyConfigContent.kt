package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.ConfigFrame
import com.zegreatrob.coupling.client.Editor
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatarLink
import com.zegreatrob.coupling.components.ConfigForm
import com.zegreatrob.coupling.components.ConfigHeader
import com.zegreatrob.coupling.components.configInput
import com.zegreatrob.coupling.components.party.PartyCard
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Color
import csstype.Display
import csstype.number
import emotion.react.css
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
    var party: Party,
    var isNew: Boolean?,
    var onChange: (ChangeEvent<*>) -> Unit,
    var onSave: () -> Unit,
    var onDelete: (() -> Unit)?,
) : DataPropsBind<PartyConfigContent>(partyConfigContent)

private val styles = useStyles("party/PartyConfig")

val partyConfigContent = tmFC<PartyConfigContent> { (party, isNew, onChange, onSave, onDelete) ->
    ConfigFrame {
        className = styles.className
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")

        ConfigHeader {
            this.party = party
            +"Party Configuration"
        }
        div {
            css { display = csstype.Display.flex }
            partyConfigEditor(party, isNew ?: false, onChange, onSave, onDelete)
            add(PartyCard(party))
        }
    }
}

private fun ChildrenBuilder.partyConfigEditor(
    updatedParty: Party,
    isNew: Boolean,
    onChange: (ChangeEvent<*>) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?
) = span {
    css {
        display = Display.inlineBlock
        flexGrow = number(2.0)
    }
    ConfigForm {
        this.onSubmit = onSave
        this.onRemove = onDelete
        editorDiv(updatedParty, onChange, isNew)
    }
}

private fun ChildrenBuilder.editorDiv(party: Party, onChange: (ChangeEvent<*>) -> Unit, isNew: Boolean) =
    div {
        Editor {
            li {
                nameInput(party, onChange)
                span { +"The full party name!" }
            }
            li {
                emailInput(party, onChange)
                span {
                    +"The party email address - Attach a"
                    gravatarLink {}
                    +"to this to cheese your party icon."
                }
            }

            if (isNew) {
                li {
                    uniqueIdInput(party, onChange)
                    span { +"This affects your party's URL. This is permanently assigned." }
                }
            }
            li {
                enableAnimationsInput(party, onChange)
                span { +"Keep things wacky and springy, or still and deadly serious." }
            }
            li {
                animationSpeedSelect(party, onChange)
                span { +"In case you want things to move a little... faster." }
            }
            li {
                enableCallSignsInput(party, onChange)
                span { +"Every Couple needs a Call Sign. Makes things more fun!" }
            }
            li {
                enableBadgesInput(party, onChange)
                span { +"Advanced users only: this lets you divide your party into two groups." }
            }
            li {
                defaultBadgeInput(party, onChange)
                span { +"The first badge a player can be given. When badges are enabled, existing players default to having this badge." }
            }
            li {
                altBadgeInput(party, onChange)
                span { +"The other badge a player can be given. A player can only have one badge at a time." }
            }
            li {
                pairingRuleSelect(party, onChange)
                span { +"Advanced users only: This rule affects how players are assigned." }
            }
        }
    }

private fun ChildrenBuilder.animationSpeedSelect(party: Party, onChange: ChangeEventHandler<HTMLSelectElement>) {
    label {
        htmlFor = "animation-speed"
        +"Animation Speed"
    }
    select {
        id = "animation-speed"
        name = "animationSpeed"
        this.value = "${party.animationSpeed}"
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

private fun ChildrenBuilder.pairingRuleSelect(party: Party, onChange: (ChangeEvent<*>) -> Unit) {
    label {
        htmlFor = "pairing-rule"
        +"Pairing Rule"
    }
    select {
        id = "pairing-rule"
        name = "pairingRule"
        this.value = "${PairingRule.toValue(party.pairingRule)}"
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

private fun ChildrenBuilder.altBadgeInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Alt Badge Name",
    id = "alt-badge-name",
    name = "alternateBadgeName",
    value = party.alternateBadgeName,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.defaultBadgeInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Default Badge Name",
    id = "default-badge-name",
    name = "defaultBadgeName",
    value = party.defaultBadgeName,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.enableBadgesInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Badges",
    id = "badge-checkbox",
    name = "badgesEnabled",
    value = party.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = party.badgesEnabled,
)

private fun ChildrenBuilder.enableAnimationsInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Animations",
    id = "animations-checkbox",
    name = "animationsEnabled",
    value = party.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = party.animationEnabled,
)

private fun ChildrenBuilder.enableCallSignsInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Enable Call Signs",
    id = "call-sign-checkbox",
    name = "callSignsEnabled",
    value = party.id.value,
    type = InputType.checkbox,
    onChange = onChange,
    checked = party.callSignsEnabled,
)

private fun ChildrenBuilder.uniqueIdInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Unique Id",
    id = "tribe-id",
    name = "id",
    value = party.id.value,
    type = InputType.text,
    onChange = onChange,
)

private fun ChildrenBuilder.emailInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Email",
    id = "tribe-email",
    name = "email",
    value = party.email ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the party email here",
)

private fun ChildrenBuilder.nameInput(party: Party, onChange: (ChangeEvent<*>) -> Unit) = configInput(
    labelText = "Name",
    id = "tribe-name",
    name = "name",
    value = party.name ?: "",
    type = InputType.text,
    onChange = onChange,
    placeholder = "Enter the party name here",
    autoFocus = true
)

private val pairingRuleDescriptions = mapOf(
    PairingRule.LongestTime to "Prefer Longest Time",
    PairingRule.PreferDifferentBadge to "Prefer Different Badges (Beta)"
)
