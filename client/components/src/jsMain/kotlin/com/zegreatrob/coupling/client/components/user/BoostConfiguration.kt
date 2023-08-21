package com.zegreatrob.coupling.client.components.user

import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.components.loadMarkdownString
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.h4
import react.dom.html.ReactHTML.p

external interface BoostConfigurationProps : Props {
    var subscription: SubscriptionDetails?
    var boost: Boost?
    var parties: List<PartyDetails>
}

@ReactFunc
val BoostConfiguration by nfc<BoostConfigurationProps> { props ->
    val subscription = props.subscription
    if (subscription?.isActive == true) {
        Markdown { +loadMarkdownString("Boost") }

        val selectedParty = props.parties.firstOrNull { props.boost?.partyIds?.contains(it.id) == true }

        h4 { +"Currently Boosting:" }
        p { +selectedParty?.name }
    }
}
