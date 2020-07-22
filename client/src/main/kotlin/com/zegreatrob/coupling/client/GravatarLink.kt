package com.zegreatrob.coupling.client

import kotlinx.css.em
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import react.RBuilder
import styled.css
import styled.styledA

fun RBuilder.gravatarLink() = styledA(href = "https://en.gravatar.com/", target = "_blank") {
    css {
        marginLeft = 0.4.em
        marginRight = 0.4.em
    }
    +"Gravatar"
}