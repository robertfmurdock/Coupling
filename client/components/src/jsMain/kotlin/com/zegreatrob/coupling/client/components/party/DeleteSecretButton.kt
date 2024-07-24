package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.secret.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.lightGreen
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState
import web.cssom.ClassName
import web.html.InputType

external interface DeleteSecretButtonProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var secret: Secret
    var dispatcher: DispatchFunc<DeleteSecretCommand.Dispatcher>
    var onSuccess: () -> Unit
}

@ReactFunc
val DeleteSecretButton by nfc<DeleteSecretButtonProps> { props ->
    CouplingButton(
        sizeRuleSet = large,
        colorRuleSet = red,
        onClick = props.dispatcher {
            fire(DeleteSecretCommand(props.partyId, props.secret.id))
            props.onSuccess()
        },
    ) {
        i { className = ClassName("fa fa-trash") }
    }
}

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
    label {
        +"Description"
        input {
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
    }

    label {
        +"Secret ID"
        input {
            type = InputType.text
            disabled = true
            value = createdSecret?.id ?: ""
        }
    }
    label {
        +"Secret Value"
        input {
            type = InputType.text
            disabled = true
            value = secretValue
        }
    }
}
