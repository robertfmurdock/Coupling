package com.zegreatrob.coupling.client.dom

import com.zegreatrob.coupling.client.cssButton
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.children
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.boxShadow
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
val lightGreen: RuleSet = {
    backgroundColor = Color("#7fd8be")
    color = Color("#3e474c")
    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#68b39d")
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
    backgroundColor = Color("#345995")
    rule(":hover") {
        backgroundColor = Color("#5188e1")
    }
}
val white: RuleSet = {
    backgroundColor = Color("#f3ffff")
    color = Color("#3e474c")
    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#cdd7d7")
    }
}
val yellow: RuleSet = {
    backgroundColor = Color("#eac435")
    color = Color("#3e474c")
    put("text-shadow", "none")
    rule(":hover") {
        backgroundColor = Color("#cbaa2d")
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
    val className: String = "",
    val onClick: () -> Unit = {},
    val attrs: BUTTON.() -> Unit = {},
    val css: CssBuilder.() -> Unit = {}
) : DataProps<CouplingButton> {
    override val component: TMFC<CouplingButton> get() = couplingButton
}

val couplingButton = tmFC<CouplingButton> { props ->
    val (sizeRuleSet, colorRuleSet, className, onClick, block, css) = props
    cssButton(
        attrs = {
            classes = classes + "button" + className
            type = ButtonType.button
            onClickFunction = { onClick() }
            block()
        },
        css = {
            buttonRuleset()
            sizeRuleSet()
            colorRuleSet()
            css()
        }
    ) {
        children(props)
    }
}
