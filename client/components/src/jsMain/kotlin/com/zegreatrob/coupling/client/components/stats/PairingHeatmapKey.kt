package com.zegreatrob.coupling.client.components.stats

import csstype.PropertiesBuilder
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.strong
import web.aria.AriaRole
import web.aria.group
import web.cssom.AlignItems
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.BoxSizing
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.LineStyle
import web.cssom.None
import web.cssom.deg
import web.cssom.linearGradient
import web.cssom.px

internal fun PropertiesBuilder.heatmapInformationPanel(accent: Color) {
    boxSizing = BoxSizing.borderBox
    minWidth = 0.px
    border = Border(2.px, LineStyle.solid, accent)
    borderRadius = 10.px
    backgroundColor = Color("#fffdf8")
    boxShadow = BoxShadow(3.px, 3.px, Color("#b9a6c7"))
    "@media (forced-colors: active)" {
        color = Color("CanvasText")
        backgroundColor = Color("Canvas")
        borderColor = Color("CanvasText")
        boxShadow = None.none
    }
}

internal fun ChildrenBuilder.heatmapKey() = div {
    role = AriaRole.group
    ariaLabel = "Heatmap key"
    css {
        heatmapInformationPanel(Color("#42688e"))
        padding = 10.px
    }
    div {
        css {
            display = Display.flex
            flexWrap = FlexWrap.wrap
            alignItems = AlignItems.center
            justifyContent = JustifyContent.spaceBetween
            gap = 8.px
        }
        strong {
            css {
                fontSize = 12.px
                color = Color("#254d75")
            }
            +"Heatmap key"
        }
        div {
            css {
                display = Display.flex
                flexWrap = FlexWrap.wrap
                gap = 10.px
                fontSize = 12.px
            }
            noCountKey()
        }
    }
    div {
        ariaHidden = true
        css {
            height = 12.px
            marginTop = 7.px
            borderRadius = 5.px
            border = Border(1.px, LineStyle.solid, Color("#34214f"))
            backgroundImage = linearGradient(
                90.deg,
                Color("#2c7bb6"), Color("#00a6ca"), Color("#00ccbc"),
                Color("#90eb9d"), Color("#ffff8c"), Color("#f9d057"),
                Color("#f29e2e"), Color("#e76818"), Color("#d7191c"),
            )
        }
    }
    div {
        css {
            display = Display.flex
            justifyContent = JustifyContent.spaceBetween
            fontSize = 11.px
            marginTop = 3.px
        }
        span { +"0 pairings" }
        span { +"5+ pairings" }
    }
}

private fun ChildrenBuilder.noCountKey() = span {
    css {
        display = Display.inlineFlex
        alignItems = AlignItems.center
        gap = 5.px
    }
    span {
        ariaHidden = true
        css {
            display = Display.inlineBlock
            width = 13.px
            height = 13.px
            backgroundColor = Color("#eee")
            border = Border(1.px, LineStyle.solid, Color("#777"))
        }
    }
    +"No count"
}
