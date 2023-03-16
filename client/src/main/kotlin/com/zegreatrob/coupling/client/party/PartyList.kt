package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.DemoButton
import com.zegreatrob.coupling.components.GqlButton
import com.zegreatrob.coupling.components.LogoutButton
import com.zegreatrob.coupling.components.NotificationButton
import com.zegreatrob.coupling.components.PageFrame
import com.zegreatrob.coupling.components.green
import com.zegreatrob.coupling.components.party.PartyCard
import com.zegreatrob.coupling.components.supersize
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Color
import csstype.VerticalAlign
import csstype.px
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class PartyList(val parties: List<Party>) : DataPropsBind<PartyList>(partyList)

val partyList = tmFC<PartyList> { (parties) ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
            className = emotion.css.ClassName {
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

val NewPartyButton = FC<Props> {
    Link {
        to = "/new-party/"
        draggable = false
        tabIndex = -1
        add(CouplingButton(supersize, green)) { +"Form a new party!" }
    }
}
