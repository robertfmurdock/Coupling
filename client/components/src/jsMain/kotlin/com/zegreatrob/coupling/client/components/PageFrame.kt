package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.px
import web.cssom.vh

external interface PageFrameProps : PropsWithChildren {
    var borderColor: Color
    var backgroundColor: Color
    var className: ClassName?
}

@ReactFunc
val PageFrame by nfc<PageFrameProps> { props ->
    div {
        css(props.className) {
            padding = Padding(0.px, 25.px, 25.px, 25.px)
            borderStyle = LineStyle.Companion.solid
            borderTopWidth = 2.px
            borderBottomWidth = 2.px
            borderLeftWidth = 12.px
            borderRightWidth = 12.px
            borderRadius = 82.px
            margin = Margin(0.px, 20.px)
            display = Display.Companion.inlineBlock
            minHeight = 100.vh
            this.borderColor = props.borderColor
            this.backgroundColor = props.backgroundColor
        }
        +props.children
    }
}
