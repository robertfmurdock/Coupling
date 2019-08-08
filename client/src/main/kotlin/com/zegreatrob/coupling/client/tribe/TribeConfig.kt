package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toTribe
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

object TribeConfig : ComponentProvider<TribeConfigProps>(), TribeConfigBuilder

data class TribeConfigProps(val tribe: KtTribe, val pathSetter: (String) -> Unit) : RProps

external interface TribeConfigStyles {
    val saveButton: String
    val editor: String
    val className: String
}

typealias TribeConfigRenderer = ScopedPropsStylesBuilder<TribeConfigProps, TribeConfigStyles>

interface TribeConfigBuilder : ScopedStyledComponentBuilder<TribeConfigProps, TribeConfigStyles>,
    UseFormHook, SaveTribeCommandDispatcher, DeleteTribeCommandDispatcher {

    override val componentPath: String get() = "tribe/TribeConfig"

    override fun build() = buildBy {
        val isNew = props.tribe.id.value == ""

        val (values, onChange) = useForm(props.tribe.toJson())
        val updatedTribe = values.toTribe()

        return@buildBy {
            div(classes = styles.className) {
                div {
                    h1 { +"Tribe Configuration" }
                }

                tribeForm(updatedTribe, isNew, onChange)()

                div {
                    input(InputType.button, classes = "super blue button") {
                        attrs {
                            classes += styles.saveButton
                            tabIndex = "0"
                            value = "Save"
                            onClickFunction = { onClickSave(updatedTribe) }
                        }
                    }
                    if (!isNew) {
                        div(classes = "small red button delete-tribe-button") {
                            attrs { onClickFunction = { onClickDelete() } }
                            +"Retire"
                        }
                    }
                }
            }
        }
    }

    private fun TribeConfigRenderer.onClickDelete() = scope.launch {
        DeleteTribeCommand(props.tribe.id).perform()
        props.pathSetter("/tribes/")
    }

    private fun TribeConfigRenderer.onClickSave(updatedTribe: KtTribe) = scope.launch {
        SaveTribeCommand(updatedTribe).perform()
        props.pathSetter("/tribes/")
    }

    private fun TribeConfigRenderer.tribeForm(
        tribe: KtTribe,
        isNew: Boolean,
        onChange: (Event) -> Unit
    ): RBuilder.() -> ReactElement = {
        div {
            span {
                this@tribeForm.configInputList(this, tribe, onChange, isNew)
            }
            tribeCard(TribeCardProps(tribe, pathSetter = props.pathSetter))
        }
    }

    private fun TribeConfigRenderer.configInputList(
        rBuilder: RBuilder,
        tribe: KtTribe,
        onChange: (Event) -> Unit,
        isNew: Boolean
    ) = rBuilder.ul(classes = styles.editor) {
        li {
            configInput(
                labelText = "Name",
                id = "tribe-name",
                name = "name",
                value = tribe.name ?: "",
                type = InputType.text,
                onChange = onChange,
                placeholder = "Enter the tribe name here"
            )
            span { +"The full tribe name!" }
        }
        li {
            configInput(
                labelText = "Email",
                id = "tribe-email",
                name = "email",
                value = tribe.email ?: "",
                type = InputType.text,
                onChange = onChange,
                placeholder = "Enter the tribe email here"
            )
            span { +"The tribe email address - Attach a Gravatar to this to cheese your tribe icon." }
        }

        if (isNew) {
            li {
                configInput(
                    labelText = "Unique Id",
                    id = "tribe-id",
                    name = "id",
                    value = tribe.id.value,
                    type = InputType.text,
                    onChange = onChange
                )
            }
        }
        li {
            configInput(
                labelText = "Enable Call Signs",
                id = "call-sign-checkbox",
                name = "callSignsEnabled",
                value = tribe.id.value,
                type = InputType.checkBox,
                onChange = onChange,
                checked = tribe.callSignsEnabled
            )
            span { +"Every Couple needs a Call Sign. Makes things more fun!" }
        }
        li {
            configInput(
                labelText = "Enable Badges",
                id = "badge-checkbox",
                name = "badgesEnabled",
                value = tribe.id.value,
                type = InputType.checkBox,
                onChange = onChange,
                checked = tribe.badgesEnabled
            )
            span { +"Advanced users only: this lets you divide your tribe into two groups." }
        }
        li {
            configInput(
                labelText = "Default Badge Name",
                id = "default-badge-name",
                name = "defaultBadgeName",
                value = tribe.defaultBadgeName ?: "",
                type = InputType.text,
                onChange = onChange
            )
            span { +"The first badge a player can be given. When badges are enabled, existing players default to having this badge." }
        }
        li {
            configInput(
                labelText = "Alt Badge Name",
                id = "alt-badge-name",
                name = "alternateBadgeName",
                value = tribe.alternateBadgeName ?: "",
                type = InputType.text,
                onChange = onChange
            )
            span { +"The other badge a player can be given. A player can only have one badge at a time." }
        }
        li {
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
            span { +"Advanced users only: This rule affects how players are assigned." }
        }
    }
}

val pairingRuleDescriptions = mapOf(
    PairingRule.LongestTime to "Prefer Longest Time",
    PairingRule.PreferDifferentBadge to "Prefer Different Badges (Beta)"
)
