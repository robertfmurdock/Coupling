package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.fitty.fitty
import com.zegreatrob.minreact.nfc
import csstype.AlignItems
import csstype.Display
import csstype.FlexDirection
import csstype.Margin
import csstype.Overflow
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import react.PropsWithClassName
import react.dom.html.ReactHTML.div
import react.useLayoutEffect
import react.useRef
import web.html.HTMLDivElement

external interface CardHeaderProps : PropsWithClassName {
    var size: Int
    var headerContent: String
}

val CardHeader by nfc<CardHeaderProps> { props ->
    val size = props.size
    val headerContainerRef = useRef<HTMLDivElement>(null)
    useLayoutEffect { headerContainerRef.current?.fitContent(size) }
    div {
        css(props.className) {
            margin = Margin((size * 0.02).px, 0.px)
            height = (size * 0.33).px
            borderRadius = (size / 10).px
            overflow = Overflow.hidden
            verticalAlign = VerticalAlign.top
            display = Display.flex
            alignItems = AlignItems.center
            flexDirection = FlexDirection.column
        }
        div {
            css { height = (size * 0.33).px }
            ref = headerContainerRef
            div {
                css {
                    display = Display.flex
                    alignItems = AlignItems.center
                    height = (size * 0.33).px
                }
                +props.headerContent.ifBlank { "Unknown" }
            }
        }
    }
}

private fun HTMLDivElement.fitContent(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.10),
    multiLine = true,
)
