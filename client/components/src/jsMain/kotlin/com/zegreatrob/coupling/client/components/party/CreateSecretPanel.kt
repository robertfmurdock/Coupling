package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.lightGreen
import com.zegreatrob.coupling.client.components.stats.CouplingInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML
import react.useState
import web.clipboard.writeText
import web.cssom.ClassName
import web.html.InputType
import web.html.text
import web.navigator.navigator
import web.prompts.alert

external interface CreateSecretPanelProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var dispatcher: DispatchFunc<CreateSecretCommand.Dispatcher>
}

@ReactFunc
val CreateSecretPanel by nfc<CreateSecretPanelProps> { props ->
    var description by useState("")
    var secretValue by useState("")
    var createdSecret by useState<Secret?>(null)
    ReactHTML.div {
        ReactHTML.h2 { +"Need another secret?" }
        ReactHTML.h4 { +"Enter a description, and then hit the create button." }
        CouplingInput {
            label = ReactNode("Description")
            backgroundColor = partySecretBackgroundColor
            inputProps = unsafeJso {
                type = InputType.Companion.text
                onChange = { event -> description = event.target.value }
            }
        }

        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = lightGreen
            onClick = props.dispatcher {
                if (description.isBlank()) {
                    alert("Please enter a description for your secret.")
                } else {
                    val result = fire(CreateSecretCommand(props.partyId, description))
                    createdSecret = result?.first
                    secretValue = result?.second ?: ""
                }
            }
            ReactHTML.i { className = ClassName("fa fa-plus") }
            +"Create New Secret"
        }

        ReactHTML.h4 { +"Your secret value will show up in the value box below." }
        +"Copy it quick! This will be your only chance to see it."
        ReactHTML.div {
            CouplingInput {
                label = ReactNode("Secret ID")
                backgroundColor = partySecretBackgroundColor
                inputProps = unsafeJso {
                    type = InputType.Companion.text
                    disabled = true
                    value = createdSecret?.id?.value?.toString() ?: ""
                }
            }
        }
        ReactHTML.div {
            CouplingInput {
                label = ReactNode("Secret Value")
                backgroundColor = partySecretBackgroundColor
                inputProps = unsafeJso {
                    type = InputType.Companion.text
                    disabled = true
                    value = secretValue
                }
            }
            CouplingButton {
                onClick = props.dispatcher {
                    if (secretValue.isBlank()) {
                        alert("No secret to copy!")
                    } else {
                        navigator.clipboard.writeText(secretValue)
                    }
                }
                ReactHTML.i { className = ClassName("fa fa-clipboard") }
                +"Copy Secret Value"
            }
        }
    }
}
