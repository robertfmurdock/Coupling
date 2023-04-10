package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.Margin
import csstype.Padding
import csstype.px
import csstype.vh
import emotion.react.css
import react.dom.html.ReactHTML.div

data class PageFrame(val borderColor: Color, val backgroundColor: Color, val className: ClassName? = null) :
    DataPropsBind<PageFrame>(pageFrame)

val pageFrame = tmFC<PageFrame> { props ->
    div {
        css(props.className) {
            padding = Padding(0.px, 25.px, 25.px, 25.px)
            borderStyle = LineStyle.solid
            borderTopWidth = 2.px
            borderBottomWidth = 2.px
            borderLeftWidth = 12.px
            borderRightWidth = 12.px
            borderRadius = 82.px
            margin = Margin(0.px, 20.px)
            display = Display.inlineBlock
            minHeight = 100.vh
            this.borderColor = props.borderColor
            this.backgroundColor = props.backgroundColor
        }
        props.children?.let { child(it) }
    }
}
