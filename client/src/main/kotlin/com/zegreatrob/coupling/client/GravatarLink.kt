package com.zegreatrob.coupling.client

import kotlinx.css.em
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.html.tabIndex
import react.Props
import react.dom.attrs
import react.fc
import styled.css
import styled.styledA

val gravatarLink = fc<Props> {
    styledA(href = "https://en.gravatar.com/", target = "_blank") {
        css {
            marginLeft = 0.4.em
            marginRight = 0.4.em
        }
        attrs {
            tabIndex = "-1"
        }
        +"Gravatar"
    }
}
