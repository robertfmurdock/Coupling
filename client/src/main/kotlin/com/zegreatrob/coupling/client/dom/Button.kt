package com.zegreatrob.coupling.client.dom

import com.zegreatrob.coupling.client.cssButton
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FontWeight
import kotlinx.css.Position
import kotlinx.css.RuleSet
import kotlinx.css.VerticalAlign
import kotlinx.css.animationPlayState
import kotlinx.css.background
import kotlinx.css.backgroundColor
import kotlinx.css.border
import kotlinx.css.borderBottom
import kotlinx.css.borderRadius
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.lineHeight
import kotlinx.css.margin
import kotlinx.css.outlineColor
import kotlinx.css.outlineOffset
import kotlinx.css.outlineWidth
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.PlayState
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.boxShadow
import kotlinx.css.px
import kotlinx.css.textDecoration
import kotlinx.css.verticalAlign
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction

val overlay = kotlinext.js.require("overlay.png").default.unsafeCast<String>()

val buttonRuleset: RuleSet = {
    background = "url($overlay) repeat-x"
    display = Display.inlineBlock
    padding = "5px 10px 6px"
    color = Color.white
    textDecoration = TextDecoration.none
    borderRadius = 6.px
    boxShadow(Color("rgba(0, 0, 0, 0.6)"), offsetY = 1.px, blurRadius = 3.px)
    put("text-shadow", "0 -1px 1px rgba(0, 0, 0, 0.25)")
    border = "1px solid rgba(0, 0, 0, 0.25)"
    position = Position.relative
    cursor = Cursor.pointer
    put("text-fill-color", "initial")
    put("text-stroke-width", "initial")
    put("text-stroke-color", "initial")
    margin = "2px"

    visited {
        borderBottom = "1px solid rgba(0, 0, 0, 0.25)"
    }

    disabled {
        animationPlayState = PlayState.paused
    }
}

val small: RuleSet = {
    fontSize = 11.px
}
val medium: RuleSet = {
    fontSize = 13.px
    fontWeight = FontWeight.bold
    lineHeight = LineHeight("1")
}
val large: RuleSet = {
    fontSize = 14.px
    padding = "8px 14px 9px"
}
val supersize: RuleSet = {
    fontSize = 34.px
    padding = "8px 14px 9px"
}
val pink: RuleSet = {
    backgroundColor = Color("#e22092")
    hover {
        backgroundColor = Color("#c81e82")
    }
    disabled {
        backgroundColor = Color("#c81e82")
    }
}
val lightGreen: RuleSet = {
    backgroundColor = Color("#7fd8be")
    color = Color("#3e474c")
    put("text-shadow", "none")
    hover {
        backgroundColor = Color("#68b39d")
    }
}
val green: RuleSet = {
    backgroundColor = Color("#42805e")
    hover {
        backgroundColor = Color("#29533d")
    }
}
val red: RuleSet = {
    backgroundColor = Color("#e62727")
    hover {
        backgroundColor = Color("#cf2525")
    }
}
val orange: RuleSet = {
    backgroundColor = Color("#ff5c00")
    hover {
        backgroundColor = Color("#d45500")
    }
}
val blue: RuleSet = {
    backgroundColor = Color("#345995")
    hover {
        backgroundColor = Color("#5188e1")
    }
}
val white: RuleSet = {
    buttonColorsWithFocus(
        backgroundColor = Color("#f3ffff"),
        color = Color("#3e474c")
    )
    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#cdd7d7")
    }
}
val yellow: RuleSet = {
    buttonColorsWithFocus(
        backgroundColor = Color("#eac435"),
        color = Color("#3e474c")
    )

    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#cbaa2d")
    }
}

private fun CssBuilder.buttonColorsWithFocus(backgroundColor: Color, color: Color) {
    this.backgroundColor = backgroundColor
    this.color = color
    focus {
        outlineColor = Color.transparent
        outlineWidth = 2.px
        outlineOffset = 2.px
    }
}

val black: RuleSet = {
    backgroundColor = Color("#222222")
    rule(":hover") {
        backgroundColor = Color("#333")
    }
}

data class CouplingButton(
    val sizeRuleSet: RuleSet = medium,
    val colorRuleSet: RuleSet = black,
    @JsName("className")
    val className: ClassName = ClassName(""),
    val onClick: () -> Unit = {},
    val attrs: BUTTON.() -> Unit = {},
    val css: CssBuilder.() -> Unit = {}
) : DataPropsBind<CouplingButton>(couplingButton)

val couplingButton = tmFC<CouplingButton> { props ->
    val (sizeRuleSet, colorRuleSet, className, onClick, block, css) = props
    cssButton(
        attrs = {
            classes = classes + "button" + "$className"
            type = ButtonType.button
            onClickFunction = { onClick() }
            block()
        },
        css = {
            descendants {
                verticalAlign = VerticalAlign.middle
            }
            buttonRuleset()
            sizeRuleSet()
            colorRuleSet()
            css()
        }
    ) {
        children(props)
    }
}
