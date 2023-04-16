package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.client.components.svgPath
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.AlignItems
import csstype.AnimationIterationCount
import csstype.Color
import csstype.Display
import csstype.Gradient
import csstype.LinearColorStop
import csstype.NamedColor
import csstype.Position
import csstype.ident
import csstype.integer
import csstype.px
import csstype.s
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.router.dom.Link

val returnToCouplingButton by nfc<Props> {
    Link {
        to = "/parties"
        tabIndex = -1
        draggable = false
        add(
            CouplingButton(sizeRuleSet = supersize, colorRuleSet = pink) {
                animationName = ident("pulsate")
                animationDuration = 0.75.s
                animationIterationCount = AnimationIterationCount.infinite
            },
        ) {
            couplingLogo()
        }
    }
}

private val couplingLogo by nfc<Props> {
    div {
        css {
            display = Display.flex
            alignItems = AlignItems.center
        }
        div {
            css {
                position = Position.relative
                width = 38.px
                height = 36.px
            }
            div {
                css {
                    position = Position.absolute
                    zIndex = integer(10)
                }
                ReactHTML.img { src = svgPath("logo") }
            }
            div {
                css {
                    position = Position.absolute
                    width = 36.px
                    height = 36.px
                    backgroundImage = radialGradient(
                        NamedColor.yellow,
                        Color("#ffff003d"),
                        Color("#e22092"),
                    )
                    borderRadius = 75.px
                }
            }
        }
    }
}

fun radialGradient(vararg stops: LinearColorStop): Gradient = "radial-gradient($stops)".unsafeCast<Gradient>()
