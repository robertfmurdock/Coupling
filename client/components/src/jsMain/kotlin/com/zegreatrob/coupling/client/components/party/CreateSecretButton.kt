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
import js.objects.jso
import react.Props
import react.ReactNode
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useState
import web.cssom.ClassName
import web.html.InputType

external interface CreateSecretButtonProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var dispatcher: DispatchFunc<CreateSecretCommand.Dispatcher>
}

@ReactFunc
val CreateSecretButton by nfc<CreateSecretButtonProps> { props ->
    var description by useState("")
    var secretValue by useState("")
    var createdSecret by useState<Secret?>(null)
    CouplingInput {
        label = ReactNode("Description")
        backgroundColor = partySecretBackgroundColor
        inputProps = jso {
            type = InputType.text
            onChange = { event -> description = event.target.value }
        }
    }

    CouplingButton(
        sizeRuleSet = large,
        colorRuleSet = lightGreen,
        attrs = fun ButtonHTMLAttributes<*>.() {
            disabled = description.isBlank()
        },
        onClick = props.dispatcher {
            val result = fire(CreateSecretCommand(props.partyId, description))
            createdSecret = result?.first
            secretValue = result?.second ?: ""
        },
    ) {
        i { className = ClassName("fa fa-plus") }
        +"Create New Secret"
    }

    div {
        CouplingInput {
            label = ReactNode("Secret ID")
            backgroundColor = partySecretBackgroundColor
            inputProps = jso {
                type = InputType.text
                disabled = true
                value = createdSecret?.id ?: ""
            }
        }
    }
    div {
        CouplingInput {
            label = ReactNode("Secret Value")
            backgroundColor = partySecretBackgroundColor
            inputProps = jso {
                type = InputType.text
                disabled = true
                value = secretValue
            }
        }
    }
}
