package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.green
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import emotion.css.ClassName
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import web.cssom.Color
import web.cssom.VerticalAlign
import web.cssom.px

data class PartyList(val parties: List<PartyDetails>) : DataPropsBind<PartyList>(partyList)

val partyList by ntmFC<PartyList> { (parties) ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
            className = ClassName {
                "> div" { padding = 7.px }
                "*" { verticalAlign = VerticalAlign.middle }
            },
        ),
    ) {
        GeneralControlBar {
            title = "Party List"
            splashComponent = CouplingLogo.create {
                width = 72.0
                height = 48.0
            }
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
        div {
            parties.forEach { party ->
                add(PartyCard(party), key = party.id.value)
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
        add(CouplingButton(supersize, green)) { +"Form a new party!" }
    }
}
