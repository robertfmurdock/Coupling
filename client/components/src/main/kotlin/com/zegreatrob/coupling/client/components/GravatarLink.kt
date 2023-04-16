package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import csstype.em
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.a
import web.window.WindowTarget

val gravatarLink by nfc<Props> {
    a {
        css {
            marginLeft = 0.4.em
            marginRight = 0.4.em
        }
        href = "https://en.gravatar.com/"
        target = WindowTarget._blank
        tabIndex = -1
        +"Gravatar"
    }
}
