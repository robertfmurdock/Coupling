package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.span
import web.cssom.ClipPath.Companion.borderBox
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.WhiteSpace
import web.cssom.px

val visuallyHidden by nfc<PropsWithChildren> { props ->
    span {
        css {
            border = 0.px
            clipPath = borderBox
            height = 1.px
            margin = ((-1).px)
            overflow = Overflow.hidden
            padding = (0.px)
            position = Position.absolute
            whiteSpace = WhiteSpace.nowrap
            width = 1.px
        }
        +props.children
    }
}
