package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.DemoButton
import com.zegreatrob.coupling.client.GqlButton
import com.zegreatrob.coupling.client.LogoutButton
import com.zegreatrob.coupling.client.NotificationButton
import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.green
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Color
import react.ChildrenBuilder
import react.create
import react.dom.html.ReactHTML.div
import react.key
import react.router.dom.Link

data class PartyList(val parties: List<Party>) : DataPropsBind<PartyList>(partyList)

private val styles = useStyles("party/PartyList")

val partyList = tmFC<PartyList> { (parties) ->
    +PageFrame(
        borderColor = Color("rgb(94, 84, 102)"),
        backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
        styles.className
    ).create {
        GeneralControlBar {
            title = "Party List"
            splashComponent = CouplingLogo.create {
                this.width = 72.0
                this.height = 48.0
            }
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
        div {
            parties.forEach { party ->
                +PartyCard(party).create { key = party.id.value }
            }
        }
        div { newPartyButton(styles["newPartyButton"]) }
    }
}

private fun ChildrenBuilder.newPartyButton(className: ClassName) = Link {
    to = "/new-tribe/"
    draggable = false
    tabIndex = -1
    +CouplingButton(supersize, green, className).create { +"Form a new party!" }
}
