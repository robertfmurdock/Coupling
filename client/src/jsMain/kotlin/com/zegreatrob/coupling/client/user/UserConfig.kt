package com.zegreatrob.coupling.client.user

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.ConfigForm
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import web.cssom.Color
import web.cssom.Display
import web.html.InputType

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
        div {
            css { display = Display.flex }
            if (user == null) {
                div { +"User not found." }
            } else {
                ConfigForm {
                    Editor {
                        li {
                            val inputId = uuid4().toString()
                            label {
                                +"User Id"
                                htmlFor = inputId
                            }
                            input {
                                name = "id"
                                id = inputId
                                type = InputType.text
                                disabled = true
                                value = user.id
                                autoFocus = true
                            }
                        }
                        li {
                            val inputId = uuid4().toString()
                            label {
                                +"User Email"
                                htmlFor = inputId
                            }
                            input {
                                name = "email"
                                id = inputId
                                type = InputType.text
                                disabled = true
                                value = user.email
                            }
                        }
                        div { +"You are authorized for these parties:" }
                        user.authorizedPartyIds
                            .map { it.value }
                            .forEach { id ->
                                div { +"Party ID: $id" }
                            }
                    }
                }
            }
            PlayerCard(
                Player(
                    id = "",
                    name = user?.email ?: "",
                    email = user?.email ?: "",
                    avatarType = null,
                ),
            )
        }
    }
}
