package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FlexWrap
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.integer
import web.cssom.pt
import web.cssom.px
import web.cssom.rgb

val NavigationPanel = FC<PropsWithChildren> { props ->
    div {
        css {
            gridColumnStart = integer(2)
            gridColumnEnd = integer(6)
            gridRow = integer(2)
            display = Display.flex
            alignItems = AlignItems.center
            flexDirection = FlexDirection.column
        }
        div {
            css {
                display = Display.inlineFlex
                alignItems = AlignItems.center
                borderRadius = 20.px
                padding = Padding(5.px, 5.px)
                margin = Margin(2.px, 2.px)
                fontSize = 0.pt
                backgroundColor = Color("#00000014")
                boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
                flexWrap = FlexWrap.wrap
            }
            +props.children
        }
    }
}
