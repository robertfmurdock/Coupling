package com.zegreatrob.coupling.client

import csstype.em
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML.a

val gravatarLink = FC<Props> {
    a {
        css {
            marginLeft = 0.4.em
            marginRight = 0.4.em
        }
        href = "https://en.gravatar.com/"
        target = AnchorTarget._blank
        tabIndex = -1
        +"Gravatar"
    }
}
