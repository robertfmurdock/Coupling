package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.green
import com.zegreatrob.coupling.client.components.party.CouplingLogo
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PartyList(val parties: List<PartyDetails>) : DataPropsBind<PartyList>(partyList)

val partyList by ntmFC<PartyList> { (parties) ->
    PartyListFrame {
        GeneralControlBar {
            title = "Party List"
            splashComponent = Fragment.create { CouplingLogo(width = 72.0, height = 48.0) }
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
        div {
            parties.forEach { party ->
                PartyCard(party, key = party.id.value)
            }
        }
        div { NewPartyButton() }
    }
}

val NewPartyButton by nfc<Props> {
    Link {
        to = "/new-party/"
        draggable = false
        tabIndex = -1
        CouplingButton(supersize, green) { +"Form a new party!" }
    }
}
