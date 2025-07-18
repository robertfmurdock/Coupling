package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.a
import web.cssom.em
import web.window.WindowTarget
import web.window._blank

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
