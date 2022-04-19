package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.svgPath
import com.zegreatrob.minreact.child
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.Position
import kotlinx.css.alignItems
import kotlinx.css.backgroundImage
import kotlinx.css.borderRadius
import kotlinx.css.display
import kotlinx.css.height
import kotlinx.css.position
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.radialGradient
import kotlinx.css.properties.s
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.css.zIndex
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.router.dom.Link

val returnToCouplingButton = FC<Props> {
    Link {
        to = "/tribes"
        tabIndex = -1
        draggable = false
        child(
            CouplingButton(sizeRuleSet = supersize, colorRuleSet = pink) {
                animation("pulsate", 0.75.s, iterationCount = IterationCount.infinite)
            }
        ) {
            couplingLogo()
        }
    }
}

private val couplingLogo = FC<Props> {
    cssDiv(css = {
        display = Display.flex
        alignItems = Align.center
    }) {
        cssDiv(css = {
            position = Position.relative
            width = 38.px
            height = 36.px
        }) {
            cssDiv(css = {
                position = Position.absolute
                zIndex = 10
            }) { ReactHTML.img { src = svgPath("logo") } }
            cssDiv(css = {
                position = Position.absolute
                width = 36.px
                height = 36.px
                backgroundImage = radialGradient {
                    colorStop(Color.yellow)
                    colorStop(Color("#ffff003d"))
                    colorStop(Color("#e22092"))
                }
                borderRadius = 75.px
            })
        }
    }
}
