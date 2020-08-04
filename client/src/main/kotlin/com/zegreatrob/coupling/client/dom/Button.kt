package com.zegreatrob.coupling.client.dom

import com.zegreatrob.minreact.reactFunction
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.boxShadow
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RHandler
import react.RProps
import styled.StyledDOMBuilder
import styled.css
import styled.styledButton

val overlay = kotlinext.js.require("overlay.png").default.unsafeCast<String>()
    .let { "/app/build/$it" }

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

    rule(":visited") {
        borderBottom = "1px solid rgba(0, 0, 0, 0.25)"
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
    rule(":hover") {
        backgroundColor = Color("#c81e82")
    }
}
val green: RuleSet = {
    backgroundColor = Color("#42805e")
    rule(":hover") {
        backgroundColor = Color("#29533d")
    }
}
val red: RuleSet = {
    backgroundColor = Color("#e62727")
    rule(":hover") {
        backgroundColor = Color("#cf2525")
    }
}
val orange: RuleSet = {
    backgroundColor = Color("#ff5c00")
    rule(":hover") {
        backgroundColor = Color("#d45500")
    }
}
val blue: RuleSet = {
    backgroundColor = Color("#2981e4")
    rule(":hover") {
        backgroundColor = Color("#2575cf")
    }
}
val white: RuleSet = {
    backgroundColor = Color("#f3ffff")
    color = Color("#3e474c")
    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#dde9e9")
    }
}
val yellow: RuleSet = {
    backgroundColor = Color("#ffb515")
    rule(":hover") {
        backgroundColor = Color("#fc9200")
    }
}
val black: RuleSet = {
    backgroundColor = Color("#222222")
    rule(":hover") {
        backgroundColor = Color("#333")
    }
}

fun RBuilder.couplingButton(
    sizeRuleSet: RuleSet = medium,
    colorRuleSet: RuleSet = black,
    className: String = "",
    onClick: () -> Unit = {},
    block: StyledDOMBuilder<BUTTON>.() -> Unit = {},
    handler: RHandler<CouplingButtonProps> = {}
) = child(CouplingButton, CouplingButtonProps(sizeRuleSet, colorRuleSet, className, onClick, block), handler)

data class CouplingButtonProps(
    val sizeRuleSet: RuleSet = medium,
    val colorRuleSet: RuleSet = black,
    val className: String = "",
    val onClick: () -> Unit = {},
    val block: StyledDOMBuilder<BUTTON>.() -> Unit = {}
) : RProps

val CouplingButton = reactFunction<CouplingButtonProps> { props ->
    val (sizeRuleSet, colorRuleSet, className, onClick, block) = props
    styledButton {
        css(buttonRuleset)
        css(sizeRuleSet)
        css(colorRuleSet)
        attrs {
            classes += "button"
            classes += className
            type = ButtonType.button
            onClickFunction = { onClick() }
        }
        block()
        props.children()
    }

}
