package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.client.party.GeneralControlBar
import com.zegreatrob.coupling.components.DemoButton
import com.zegreatrob.coupling.components.GqlButton
import com.zegreatrob.coupling.components.LogoutButton
import com.zegreatrob.coupling.components.NotificationButton
import com.zegreatrob.coupling.components.PageFrame
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Color
import react.dom.html.ReactHTML.div

data class UserConfig(val user: User) : DataProps<UserConfig> {
    override val component = userConfig
}

private val userConfig = tmFC<UserConfig> { (user) ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("floralwhite"),
        ),
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
