package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.fitty.fitty
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.Overflow
import kotlinx.css.VerticalAlign
import kotlinx.css.alignItems
import kotlinx.css.borderRadius
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.overflow
import kotlinx.css.px
import kotlinx.css.verticalAlign
import org.w3c.dom.Node
import react.FC
import react.PropsWithClassName
import react.ref
import react.useLayoutEffect
import react.useRef

external interface CardHeaderProps : PropsWithClassName {
    var size: Int
    var headerContent: String
}

val CardHeader = FC<CardHeaderProps> { props ->
    val size = props.size
    val headerContainerRef = useRef<Node>(null)
    useLayoutEffect { headerContainerRef.current?.fitContent(size) }
    cssDiv(
        props = { className = props.className },
        css = {
            margin((size * 0.02).px, 0.px)
            height = (size * 0.33).px
            borderRadius = (size / 10).px
            overflow = Overflow.hidden
            verticalAlign = VerticalAlign.top
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.column
        }
    ) {
        cssDiv(
            props = { ref = headerContainerRef },
            css = { height = (size * 0.33).px }
        ) {
            cssDiv(css = {
                display = Display.flex
                alignItems = Align.center
                height = (size * 0.33).px
            }) {
                +props.headerContent.ifBlank { "Unknown" }
            }
        }
    }
}

private fun Node.fitContent(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.10),
    multiLine = true
)
