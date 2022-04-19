package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.backgroundColor
import kotlinx.css.borderBottomWidth
import kotlinx.css.borderColor
import kotlinx.css.borderLeftWidth
import kotlinx.css.borderRadius
import kotlinx.css.borderRightWidth
import kotlinx.css.borderStyle
import kotlinx.css.borderTopWidth
import kotlinx.css.display
import kotlinx.css.margin
import kotlinx.css.minHeight
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.vh
import kotlinx.html.classes

data class PageFrame(val borderColor: Color, val backgroundColor: Color, val className: ClassName? = null) :
    DataPropsBind<PageFrame>(pageFrame)

val pageFrame = tmFC<PageFrame> { props ->
    cssDiv(css = {
        padding(0.px, 25.px, 25.px, 25.px)
        borderStyle = BorderStyle.solid
        borderTopWidth = 2.px
        borderBottomWidth = 2.px
        borderLeftWidth = 12.px
        borderRightWidth = 12.px
        borderRadius = 82.px
        margin(0.px, 20.px)
        display = Display.inlineBlock
        minHeight = 100.vh
        this.borderColor = props.borderColor
        this.backgroundColor = props.backgroundColor
    }, attrs = {
        props.className?.let { classes = classes + "$it" }
    }) {
        props.children?.let { child(it) }
    }
}
