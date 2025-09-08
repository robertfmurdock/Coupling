package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML
import web.cssom.Color

@ReactFunc
external interface PartySecretsLayoutProps<D> : Props where D : DeleteSecretCommand.Dispatcher, D : CreateSecretCommand.Dispatcher {
    var partyDetails: PartyDetails
    var secrets: List<Secret>
    var boost: Boost?
    var dispatcher: DispatchFunc<D>
    var reload: () -> Unit
}

val partySecretBackgroundColor = Color("hsla(45, 80%, 96%, 1)")

@ReactFunc
val PartySecretLayout by nfc<PartySecretsLayoutProps<*>> { props ->
    val party = props.partyDetails
    val dispatcher = props.dispatcher
    ConfigFrame {
        backgroundColor = partySecretBackgroundColor
        borderColor = Color("#ff8c00")
        ConfigHeader(party = party, boost = props.boost) {
            +"Party Secrets"
        }
        ReactHTML.div {
            if (props.secrets.isEmpty()) {
                NoSecretsView()
            } else {
                SecretView(party.id, props.secrets, dispatcher, props.reload)
            }
            ReactHTML.hr()
            CreateSecretPanel(party.id, dispatcher)
        }
    }
}
