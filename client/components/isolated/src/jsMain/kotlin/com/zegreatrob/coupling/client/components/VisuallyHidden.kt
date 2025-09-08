package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML
import web.cssom.ClipPath
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.WhiteSpace
import web.cssom.px

val visuallyHidden by nfc<PropsWithChildren> { props ->
    ReactHTML.span {
        css {
            border = 0.px
            clipPath = ClipPath.Companion.borderBox
            height = 1.px
            margin = ((-1).px)
            overflow = Overflow.Companion.hidden
            padding = (0.px)
            position = Position.Companion.absolute
            whiteSpace = WhiteSpace.Companion.nowrap
            width = 1.px
        }
        +props.children
    }
}
