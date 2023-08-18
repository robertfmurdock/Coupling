package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.secret.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import web.cssom.ClassName

external interface DeleteSecretButtonProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var secret: Secret
    var dispatcher: DispatchFunc<DeleteSecretCommand.Dispatcher>
}

@ReactFunc
val DeleteSecretButton by nfc<DeleteSecretButtonProps> { props ->
    CouplingButton(
        sizeRuleSet = large,
        colorRuleSet = white,
        onClick = props.dispatcher { fire(DeleteSecretCommand(props.partyId, props.secret)) },
    ) {
        i { className = ClassName("fa fa-trash") }
    }
}
