package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

external interface UserConfigProps : Props {
    var user: User?
}

@ReactFunc
val UserConfig by nfc<UserConfigProps> { (user) ->
    PageFrame(
        borderColor = Color("rgb(94, 84, 102)"),
        backgroundColor = Color("floralwhite"),
    ) {
        GeneralControlBar {
            title = "User Config"
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
        if (user == null) {
            div { +"User not found." }
        } else {
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
}
