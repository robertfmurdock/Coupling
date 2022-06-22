package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.components.external.fitty.fitty
import csstype.AlignItems
import csstype.Margin
import csstype.px
import emotion.react.css
import org.w3c.dom.Node
import react.FC
import react.PropsWithClassName
import react.dom.html.ReactHTML.div
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
    div {
        css(props.className) {
            margin = Margin((size * 0.02).px, 0.px)
            height = (size * 0.33).px
            borderRadius = (size / 10).px
            overflow = csstype.Overflow.hidden
            verticalAlign = csstype.VerticalAlign.top
            display = csstype.Display.flex
            alignItems = AlignItems.center
            flexDirection = csstype.FlexDirection.column
        }
        div {
            css { height = (size * 0.33).px }
            ref = headerContainerRef
            div {
                css {
                    display = csstype.Display.flex
                    alignItems = AlignItems.center
                    height = (size * 0.33).px
                }
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
