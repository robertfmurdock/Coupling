package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.ntmFC
import csstype.PropertiesBuilder
import emotion.react.css
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.button
import web.cssom.AnimationPlayState
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.LineStyle.Companion.solid
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Padding
import web.cssom.Position
import web.cssom.TextShadow
import web.cssom.VerticalAlign
import web.cssom.number
import web.cssom.px
import web.cssom.rgba
import web.cssom.url
import web.html.ButtonType

val buttonRuleset: PropertiesBuilder.() -> Unit = {
    backgroundImage = url(pngPath("overlay"))
    backgroundRepeat = BackgroundRepeat.repeatX

    display = Display.inlineBlock
    padding = Padding(5.px, 10.px, 6.px)
    color = NamedColor.white
    textDecoration = None.none
    borderRadius = 6.px
    boxShadow = BoxShadow(offsetX = 0.px, offsetY = 1.px, blurRadius = 3.px, color = rgba(0, 0, 0, 0.6))
    textShadow = TextShadow(0.px, (-1).px, 1.px, rgba(0, 0, 0, 0.25))
    border = Border(1.px, solid, rgba(0, 0, 0, 0.25))
    position = Position.relative
    cursor = Cursor.pointer
    asDynamic()["text-fill-color"] = "initial"
    asDynamic()["text-stroke-width"] = "initial"
    asDynamic()["text-stroke-color"] = "initial"
    margin = 2.px

    visited {
        borderBottom = Border(1.px, solid, rgba(0, 0, 0, 0.25))
    }

    disabled {
        animationPlayState = AnimationPlayState.paused
    }
}

val small: PropertiesBuilder.() -> Unit = {
    fontSize = 11.px
}
val medium: PropertiesBuilder.() -> Unit = {
    fontSize = 13.px
    fontWeight = FontWeight.bold
    lineHeight = number(1.0)
}
val large: PropertiesBuilder.() -> Unit = {
    fontSize = 14.px
    padding = Padding(8.px, 14.px, 9.px)
}
val supersize: PropertiesBuilder.() -> Unit = {
    fontSize = 34.px
    padding = Padding(8.px, 14.px, 9.px)
}
val pink: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#e22092")
    hover {
        backgroundColor = Color("#c81e82")
    }
    disabled {
        backgroundColor = Color("#c81e82")
    }
}
val lightGreen: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#7fd8be")
    color = Color("#3e474c")
    textShadow = None.none
    hover {
        backgroundColor = Color("#68b39d")
    }
}
val green: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#42805e")
    hover {
        backgroundColor = Color("#29533d")
    }
}
val red: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#e62727")
    hover {
        backgroundColor = Color("#cf2525")
    }
}
val orange: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#ff5c00")
    hover {
        backgroundColor = Color("#d45500")
    }
}
val blue: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#345995")
    hover {
        backgroundColor = Color("#5188e1")
    }
}
val white: PropertiesBuilder.() -> Unit = {
    buttonColorsWithFocus(
        backgroundColor = Color("#f3ffff"),
        color = Color("#3e474c"),
    )
    textShadow = None.none
    ":hover" {
        backgroundColor = Color("#cdd7d7")
    }
}
val yellow: PropertiesBuilder.() -> Unit = {
    buttonColorsWithFocus(
        backgroundColor = Color("#eac435"),
        color = Color("#3e474c"),
    )
    textShadow = None.none
    ":hover" {
        backgroundColor = Color("#cbaa2d")
    }
}

private fun PropertiesBuilder.buttonColorsWithFocus(backgroundColor: Color, color: Color) {
    this.backgroundColor = backgroundColor
    this.color = color
    focus {
        outlineColor = NamedColor.transparent
        outlineWidth = 2.px
        outlineOffset = 2.px
    }
}

val black: PropertiesBuilder.() -> Unit = {
    backgroundColor = Color("#222222")
    ":hover" {
        backgroundColor = Color("#333")
    }
}

data class CouplingButton(
    val sizeRuleSet: PropertiesBuilder.() -> Unit = medium,
    val colorRuleSet: PropertiesBuilder.() -> Unit = black,
    @JsName("className")
    val className: ClassName = ClassName(""),
    val onClick: () -> Unit = {},
    val attrs: ButtonHTMLAttributes<*>.() -> Unit = {},
    val css: PropertiesBuilder.() -> Unit = {},
) : DataPropsBind<CouplingButton>(couplingButton)

val couplingButton by ntmFC<CouplingButton> { props ->
    val (sizeRuleSet, colorRuleSet, className, onClick, block, css) = props
    button {
        type = ButtonType.button
        this.onClick = { onClick() }
        block(this)

        css(className, ClassName("button")) {
            "*" {
                verticalAlign = VerticalAlign.middle
            }
            buttonRuleset()
            sizeRuleSet()
            colorRuleSet()
            css()
        }

        children(props)
    }
}
