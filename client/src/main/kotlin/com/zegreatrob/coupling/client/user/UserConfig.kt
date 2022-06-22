package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.GqlButton
import com.zegreatrob.coupling.client.LogoutButton
import com.zegreatrob.coupling.client.NotificationButton
import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.client.party.GeneralControlBar
import com.zegreatrob.coupling.components.DemoButton
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Color
import react.dom.html.ReactHTML.div

private val styles = useStyles("user/UserConfig")

data class UserConfig(val user: User) : DataProps<UserConfig> {
    override val component = userConfig
}

private val userConfig = tmFC<UserConfig> { (user) ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("floralwhite"),
            className = styles.className
        )
    ) {
        GeneralControlBar {
            title = "User Config"
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }

        div { +"User ID: ${user.id}" }
        div { +"User Email: ${user.email}" }
        div { +"This user owns these parties:" }
        user.authorizedPartyIds
            .map { it.value }
            .forEach { id ->
                div { +"Party ID: $id" }
            }
    }
}
