package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.client.party.GeneralControlBar
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.Color
import react.dom.html.ReactHTML.div

private val styles = useStyles("user/UserConfig")

data class UserConfig(val user: User) : DataProps<UserConfig> {
    override val component = userConfig
}

private val userConfig = tmFC<UserConfig> { (user) ->
    child(
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
