package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.GqlButton
import com.zegreatrob.coupling.client.LogoutButton
import com.zegreatrob.coupling.client.NotificationButton
import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.DemoButton
import com.zegreatrob.coupling.components.green
import com.zegreatrob.coupling.components.supersize
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Color
import csstype.VerticalAlign
import csstype.px
import react.ChildrenBuilder
import react.create
import react.dom.html.ReactHTML.div
import react.key
import react.router.dom.Link

data class PartyList(val parties: List<Party>) : DataPropsBind<PartyList>(partyList)

private val styles = useStyles("party/PartyList")

val partyList = tmFC<PartyList> { (parties) ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
            className = emotion.css.ClassName(styles.className) {
                "> div" { padding = 7.px }
                "*" { verticalAlign = VerticalAlign.middle }
            }
        )
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
                add(PartyCard(party)) { key = party.id.value }
            }
        }
        div { newPartyButton(styles["newPartyButton"]) }
    }
}

private fun ChildrenBuilder.newPartyButton(className: ClassName) = Link {
    to = "/new-tribe/"
    draggable = false
    tabIndex = -1
    add(CouplingButton(supersize, green, className)) { +"Form a new party!" }
}
