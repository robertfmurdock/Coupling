package com.zegreatrob.coupling.client

import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.WhiteSpace
import kotlinx.css.border
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.whiteSpace
import kotlinx.css.width
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
