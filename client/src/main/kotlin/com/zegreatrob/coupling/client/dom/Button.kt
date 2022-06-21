package com.zegreatrob.coupling.client.dom

import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import csstype.AnimationPlayState
import csstype.BackgroundRepeat
import csstype.Border
import csstype.BoxShadow
import csstype.ClassName
import csstype.Color
import csstype.Cursor
import csstype.Display
import csstype.FontWeight
import csstype.LineStyle.Companion.solid
import csstype.NamedColor
import csstype.None
import csstype.Padding
import csstype.Position
import csstype.PropertiesBuilder
import csstype.TextShadow
import csstype.number
import csstype.px
import csstype.rgba
import csstype.url
import emotion.react.css
import org.w3c.dom.HTMLButtonElement
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.button

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
        color = Color("#3e474c")
    )
    textShadow = None.none
    ":hover" {
        backgroundColor = Color("#cdd7d7")
    }
}
val yellow: PropertiesBuilder.() -> Unit = {
    buttonColorsWithFocus(
        backgroundColor = Color("#eac435"),
        color = Color("#3e474c")
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
    val attrs: ButtonHTMLAttributes<HTMLButtonElement>.() -> Unit = {},
    val css: PropertiesBuilder.() -> Unit = {}
) : DataPropsBind<CouplingButton>(couplingButton)

val couplingButton = tmFC<CouplingButton> { props ->
    val (sizeRuleSet, colorRuleSet, className, onClick, block, css) = props
    button {
        type = react.dom.html.ButtonType.button
        this.onClick = { onClick() }
        block()

        css(className, ClassName("button")) {
            "*" {
                verticalAlign = csstype.VerticalAlign.middle
            }
            buttonRuleset()
            sizeRuleSet()
            colorRuleSet()
            css()
        }

        children(props)
    }
}
