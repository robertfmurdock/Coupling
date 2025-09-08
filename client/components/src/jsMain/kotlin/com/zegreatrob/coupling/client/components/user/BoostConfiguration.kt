package com.zegreatrob.coupling.client.components.user

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.dom.html.ReactHTML
import react.useState

external interface BoostConfigurationProps : Props {
    var subscription: SubscriptionDetails?
    var boost: Boost?
    var parties: List<PartyDetails>
    var dispatchFunc: DispatchFunc<ApplyBoostCommand.Dispatcher>
    var reload: () -> Unit
}

@ReactFunc
val BoostConfiguration by nfc<BoostConfigurationProps> { props ->
    val subscription = props.subscription

    var boostedParty by useState { props.parties.firstOrNull { props.boost?.partyIds?.contains(it.id) == true } }

    if (subscription?.isActive == true) {
        ReactHTML.div { dangerouslySetInnerHTML = unsafeJso { __html = parse(loadMarkdownString("Boost")) } }

        ReactHTML.h4 { +"Currently Boosting:" }
        ReactHTML.p { +(boostedParty?.name ?: "No party") }

        ReactHTML.select {
            name = "party"
            value = boostedParty?.id?.value ?: ""
            onChange = { event ->
                boostedParty = props.parties.firstOrNull { it.id.value.toString() == event.target.value }
            }

            if (boostedParty == null) {
                ReactHTML.option {
                    key = "placeholder"
                    value = ""
                    label = "Select a party to boost"
                }
            }

            props.parties.forEach { party ->
                val partyName = party.name
                ReactHTML.option {
                    key = party.id.value.toString()
                    value = party.id.value
                    label = partyName
                    if (partyName != null) {
                        ariaLabel = partyName
                    }
                }
            }
        }

        CouplingButton {
            onClick = props.dispatchFunc {
                boostedParty?.id?.let {
                    fire(ApplyBoostCommand(it))
                    props.reload()
                }
            }
            +"Apply Boost"
        }
    }
}
