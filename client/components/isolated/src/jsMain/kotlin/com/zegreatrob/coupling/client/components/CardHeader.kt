package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.fitty.fitty
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithClassName
import react.dom.html.ReactHTML
import react.raw.useLayoutEffectRaw
import react.useRef
import web.cssom.AlignItems
import web.cssom.BackgroundRepeat
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.VerticalAlign
import web.cssom.deg
import web.cssom.em
import web.cssom.integer
import web.cssom.px
import web.cssom.rgb
import web.cssom.rotatex
import web.cssom.scale
import web.cssom.url
import web.html.HTMLDivElement

external interface CardHeaderProps : PropsWithClassName {
    var size: Int
    var headerContent: String
}

val CardHeader by nfc<CardHeaderProps> { props ->
    val size = props.size
    val headerContainerRef = useRef<HTMLDivElement>(null)
    useLayoutEffectRaw({
        headerContainerRef.current?.fitContent(size);
        {}
    }, emptyArray())
    ReactHTML.div {
        css(props.className) {
            margin = Margin((size * 0.02).px, 0.px)
            height = (size * 0.33).px
            verticalAlign = VerticalAlign.Companion.top
            overflow = Overflow.Companion.visible
            display = Display.Companion.flex
            alignItems = AlignItems.Companion.center
            flexDirection = FlexDirection.Companion.column
            position = Position.Companion.relative
            transform = scale(1.1)
            perspective = 10.em
        }
        ReactHTML.div {
            css {
                position = Position.Companion.absolute
                overflow = Overflow.Companion.hidden
                borderRadius = (size / 10).px
                top = 0.px
                left = 0.px
                right = 0.px
                bottom = 0.px
                transform = rotatex(20.deg)
                backgroundColor = rgb(255, 255, 255, 0.4)
                backgroundImage = url(pngPath("overlay"))
                backgroundRepeat = BackgroundRepeat.Companion.repeatX
                borderStyle = LineStyle.Companion.hidden
                borderColor = Color("#00000054")
                borderWidth = 1.px
                fontWeight = FontWeight.Companion.bold
            }
        }
        ReactHTML.div {
            css {
                height = (size * 0.33).px
                zIndex = integer(100)
            }
            ref = headerContainerRef
            ReactHTML.div {
                css {
                    display = Display.Companion.flex
                    alignItems = AlignItems.Companion.center
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
