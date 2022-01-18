package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.GqlButton
import com.zegreatrob.coupling.client.LogoutButton
import com.zegreatrob.coupling.client.NotificationButton
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class TribeBrowser(val tribe: Tribe) : DataProps<TribeBrowser> {
    override val component: TMFC<TribeBrowser> get() = tribeBrowser
}

private val styles = useStyles("tribe/TribeBrowser")

val tribeBrowser = tmFC<TribeBrowser> { (tribe) ->
    div {
        className = styles.className
        ConfigHeader {
            this.tribe = tribe
            +(tribe.name ?: "")
        }
    }
}

private fun ChildrenBuilder.tribeControlButtons() = span {
    className = styles["controlButtons"]
    TribeSelectButton()
    LogoutButton()
    GqlButton()
    NotificationButton()
}
