package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.Padding
import web.cssom.Position
import web.cssom.em
import web.cssom.px

val CouplingDropDown = FC<PropsWithChildren> { props ->
    div {
        css {
            fontSize = 14.px
            position = Position.absolute
            +black
            padding = Padding(0.2.em, 0.5.em, 0.5.em)
            borderRadius = 1.em
        }
        +props.children
    }
}
