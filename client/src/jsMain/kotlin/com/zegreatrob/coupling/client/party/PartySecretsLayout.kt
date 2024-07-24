package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.format
import com.zegreatrob.coupling.client.components.party.CreateSecretButton
import com.zegreatrob.coupling.client.components.party.DeleteSecretButton
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.tr
import web.cssom.Color
import web.cssom.Display
import web.cssom.number

@ReactFunc
external interface PartySecretsLayoutProps<D> : Props where D : DeleteSecretCommand.Dispatcher, D : CreateSecretCommand.Dispatcher {
    var partyDetails: PartyDetails
    var secrets: List<Secret>
    var boost: Boost?
    var dispatcher: DispatchFunc<D>
    var reload: () -> Unit
}

@ReactFunc
val PartySecretLayout by nfc<PartySecretsLayoutProps<*>> { props ->
    val party = props.partyDetails
    val dispatcher = props.dispatcher
    ConfigFrame {
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")
        ConfigHeader(party = party, boost = props.boost) {
            +"Party Secrets"
        }
        div {
            css { display = Display.flex }
            span {
                css {
                    display = Display.inlineBlock
                    flexGrow = number(2.0)
                }
                h2 { +"These are secrets associated with this party." }
                p { +"The 'id' here is not the secret itself, which is only revealed at the moment of its creation." }
                p { +"If you know a secret is no longer in use, then you should remove it." }
                p { +"This ensures no nefarious agents mess with your party." }

                div {
                    CreateSecretButton(party.id, dispatcher)
                }
                div {
                    css { display = Display.flex }
                    table {
                        css { flexGrow = number(1.0) }
                        th { +"Index" }
                        th { +"Id" }
                        th { +"Description" }
                        th { +"Last Used" }
                        th { +"Delete" }
                        props.secrets.forEachIndexed { index, secret ->
                            tr {
                                td { +"$index" }
                                td { +secret.id }
                                td { +secret.description }
                                td { +(secret.lastUsedTimestamp?.format() ?: "Never used.") }
                                td {
                                    DeleteSecretButton(
                                        partyId = party.id,
                                        secret = secret,
                                        dispatcher = dispatcher,
                                        onSuccess = props.reload,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            PartyCard(party)
        }
    }
}
