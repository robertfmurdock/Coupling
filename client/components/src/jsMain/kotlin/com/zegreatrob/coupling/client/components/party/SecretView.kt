package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.format
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import web.cssom.Display
import web.cssom.number

external interface SecretViewProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var secrets: List<Secret>
    var dispatcher: DispatchFunc<DeleteSecretCommand.Dispatcher>
    var reload: () -> Unit
}

@ReactFunc
val SecretView by nfc<SecretViewProps> { props ->
    div {
        h2 { +"These are secrets associated with this party." }
        p { +"The 'id' here is not the secret itself, which is only revealed at the moment of its creation." }
        p { +"If you know a secret is no longer in use, then you should remove it." }
        p { +"This ensures no nefarious agents mess with your party." }

        div {
            css { display = Display.flex }
            table {
                css { flexGrow = number(1.0) }
                thead {
                    tr {
                        th { +"Index" }
                        th { +"Id" }
                        th { +"Description" }
                        th { +"Last Used" }
                        th { +"Delete" }
                    }
                }
                tbody {
                    props.secrets.forEachIndexed { index, secret ->
                        tr {
                            td { +"$index" }
                            td { +secret.id }
                            td { +secret.description }
                            td { +(secret.lastUsedTimestamp?.format() ?: "Never used.") }
                            td {
                                DeleteSecretButton(
                                    partyId = props.partyId,
                                    secret = secret,
                                    dispatcher = props.dispatcher,
                                    onSuccess = props.reload,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
