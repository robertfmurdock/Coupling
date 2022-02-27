package com.zegreatrob.coupling.client

import kotlinx.css.*
import react.FC
import react.PropsWithChildren

val visuallyHidden = FC<PropsWithChildren> { props ->
    cssSpan(css = {
        border = "0"
        put("clip", "rect(0 0 0 0)")
        height = 1.px
        margin((-1).px)
        overflow = Overflow.hidden
        padding(0.px)
        position = Position.absolute
        whiteSpace = WhiteSpace.nowrap
        width = 1.px
    }) {
        +props.children
    }
}