package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML
import web.cssom.Color
import web.cssom.Padding
import web.cssom.em

val CouplingDropDownElement = FC<PropsWithChildren> {
    ReactHTML.div {
        css {
            color = Color("#FFFFFF")
            padding = Padding(0.2.em, 0.5.em, 0.5.em)
            borderRadius = 1.em
            ":hover" { backgroundColor = Color("#666666") }
        }
        +it.children
    }
}
