package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import csstype.ClipPath.Companion.borderBox
import csstype.Overflow
import csstype.Position
import csstype.WhiteSpace
import csstype.px
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.span

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
