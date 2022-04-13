package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.green
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.Color
import react.ChildrenBuilder
import react.create
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class TribeList(val tribes: List<Party>) : DataPropsBind<TribeList>(tribeList)

private val styles = useStyles("tribe/TribeList")

val tribeList = tmFC<TribeList> { (tribes) ->
    child(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
            styles.className
        )
    ) {
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
            tribes.forEach { tribe ->
                child(TribeCard(tribe), key = tribe.id.value)
            }
        }
        div { newTribeButton(styles["newTribeButton"]) }
    }
}

private fun ChildrenBuilder.newTribeButton(className: ClassName) = Link {
    to = "/new-tribe/"
    draggable = false
    tabIndex = -1
    child(CouplingButton(supersize, green, className)) { +"Form a new party!" }
}
