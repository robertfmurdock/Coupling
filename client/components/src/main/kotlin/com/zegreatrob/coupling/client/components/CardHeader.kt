package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.fitty.fitty
import com.zegreatrob.minreact.nfc
import csstype.AlignItems
import csstype.BackgroundRepeat
import csstype.Color
import csstype.Display
import csstype.FlexDirection
import csstype.FontWeight
import csstype.LineStyle
import csstype.Margin
import csstype.Overflow
import csstype.Position
import csstype.VerticalAlign
import csstype.deg
import csstype.em
import csstype.integer
import csstype.px
import csstype.rgba
import csstype.rotatex
import csstype.scale
import csstype.url
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
            verticalAlign = VerticalAlign.top
            overflow = Overflow.visible
            display = Display.flex
            alignItems = AlignItems.center
            flexDirection = FlexDirection.column
            position = Position.relative
            transform = scale(1.1)
            perspective = 10.em
        }
        div {
            css {
                position = Position.absolute
                overflow = Overflow.hidden
                borderRadius = (size / 10).px
                top = 0.px
                left = 0.px
                right = 0.px
                bottom = 0.px
                transform = rotatex(20.deg)
                backgroundColor = rgba(255, 255, 255, 0.4)
                backgroundImage = url(pngPath("overlay"))
                backgroundRepeat = BackgroundRepeat.repeatX
                borderStyle = LineStyle.hidden
                borderColor = Color("#00000054")
                borderWidth = 1.px
                fontWeight = FontWeight.bold
            }
        }
        div {
            css {
                height = (size * 0.33).px
                zIndex = integer(100)
            }
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
