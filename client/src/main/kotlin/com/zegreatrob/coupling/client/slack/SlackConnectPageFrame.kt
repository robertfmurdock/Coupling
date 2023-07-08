package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.party.CouplingLogo
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.minreact.nfc
import react.Fragment
import react.PropsWithChildren
import react.create
import web.cssom.Color

val SlackConnectPageFrame by nfc<PropsWithChildren> {
    ConfigFrame {
        backgroundColor = Color("hsla(45, 80%, 96%, 1)")
        borderColor = Color("#ff8c00")
        GeneralControlBar {
            title = "Slack Connect"
            splashComponent = Fragment.create { CouplingLogo(width = 72.0, height = 48.0) }
            LogoutButton()
            GqlButton()
        }
        +it.children
    }
}
