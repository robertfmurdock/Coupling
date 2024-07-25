package com.zegreatrob.coupling.client.components

import csstype.Properties
import csstype.PropertiesBuilder
import emotion.css.ClassName
import emotion.css.cx
import js.objects.jso
import react.FC
import react.PropsWithChildren
import react.PropsWithClassName
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.button
import web.cssom.AlignItems
import web.cssom.AnimationPlayState
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.LineStyle.Companion.solid
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Padding
import web.cssom.Position
import web.cssom.TextShadow
import web.cssom.VerticalAlign
import web.cssom.number
import web.cssom.px
import web.cssom.rgb
import web.cssom.url
import web.html.ButtonType
import web.html.HTMLButtonElement

val buttonRuleset: PropertiesBuilder.() -> Unit = {
    backgroundImage = url(pngPath("overlay"))
    backgroundRepeat = BackgroundRepeat.repeatX

    display = Display.inlineFlex
    alignItems = AlignItems.center
    padding = Padding(5.px, 10.px, 6.px)
    color = NamedColor.white
    textDecoration = None.none
    borderRadius = 6.px
    boxShadow = BoxShadow(offsetX = 0.px, offsetY = 1.px, blurRadius = 3.px, color = rgb(0, 0, 0, 0.6))
    textShadow = TextShadow(0.px, (-1).px, 1.px, rgb(0, 0, 0, 0.25))
    border = Border(1.px, solid, rgb(0, 0, 0, 0.25))
    position = Position.relative
    cursor = Cursor.pointer
    asDynamic()["text-fill-color"] = "initial"
    asDynamic()["text-stroke-width"] = "initial"
    asDynamic()["text-stroke-color"] = "initial"
    margin = 2.px

    "i" {
        margin = Margin(0.px, 5.px, 0.px, 0.px)
    }

    visited {
        borderBottom = Border(1.px, solid, rgb(0, 0, 0, 0.25))
    }

    disabled {
        animationPlayState = AnimationPlayState.paused
    }
}

val small: Properties = jso {
    fontSize = 11.px
}
val medium: Properties = jso {
    fontSize = 13.px
    fontWeight = FontWeight.bold
    lineHeight = number(1.0)
}
val large: Properties = jso {
    fontSize = 14.px
    padding = Padding(8.px, 14.px, 9.px)
}
val supersize: Properties = jso {
    fontSize = 34.px
    padding = Padding(8.px, 14.px, 9.px)
}
val pink: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#e22092")
    hover {
        backgroundColor = Color("#c81e82")
    }
    disabled {
        backgroundColor = Color("#c81e82")
    }
}
val lightGreen: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#7fd8be")
    color = Color("#3e474c")
    textShadow = None.none
    hover {
        backgroundColor = Color("#68b39d")
    }
}
val green: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#42805e")
    hover {
        backgroundColor = Color("#29533d")
    }
}
val red: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#e62727")
    hover {
        backgroundColor = Color("#cf2525")
    }
}
val orange: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#ff5c00")
    hover {
        backgroundColor = Color("#d45500")
    }
}
val blue: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#345995")
    hover {
        backgroundColor = Color("#5188e1")
    }
}
val white: Properties = jso<PropertiesBuilder>().apply {
    buttonColorsWithFocus(
        backgroundColor = Color("#f3ffff"),
        color = Color("#3e474c"),
    )
    textShadow = None.none
    ":hover" {
        backgroundColor = Color("#cdd7d7")
    }
}
val yellow: Properties = jso<PropertiesBuilder>().apply {
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

val black: Properties = jso<PropertiesBuilder>().apply {
    backgroundColor = Color("#222222")
    ":hover" {
        backgroundColor = Color("#333")
    }
}

external interface CouplingButtonProps :
    PropsWithChildren,
    PropsWithClassName {
    var sizeRuleSet: Properties?
    var colorRuleSet: Properties?
    var onClick: (() -> Unit)?
    var buttonProps: ButtonHTMLAttributes<HTMLButtonElement>?
}

val CouplingButton = FC<CouplingButtonProps> { props ->
    val sizeRuleSet = props.sizeRuleSet ?: medium
    val colorRuleSet = props.colorRuleSet ?: black
    val onClick = props.onClick ?: {}

    button {
        type = ButtonType.button
        +props.buttonProps
        this.onClick = { onClick() }
        className = cx(
            this.className,
            ClassName {
                "*" { verticalAlign = VerticalAlign.middle }
                buttonRuleset()
                +sizeRuleSet
                +colorRuleSet
            },
            props.className,
        )
        +props.children
    }
}
