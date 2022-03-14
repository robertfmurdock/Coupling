package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.*
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
