package com.zegreatrob.coupling.client

import kotlinx.css.em
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.html.tabIndex
import react.RBuilder
import styled.css
import styled.styledA

fun RBuilder.gravatarLink() = styledA(href = "https://en.gravatar.com/", target = "_blank") {
    css {
        marginLeft = 0.4.em
        marginRight = 0.4.em
    }
    attrs {
        tabIndex = "-1"
    }
    +"Gravatar"
}
