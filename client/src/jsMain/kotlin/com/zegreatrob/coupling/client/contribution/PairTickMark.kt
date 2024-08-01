package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.external.nivo.AxisTickProps
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import emotion.react.css
import react.FC
import react.dom.html.ReactHTML.div
import react.dom.svg.ReactSVG.foreignObject
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.rect
import react.useContext
import web.cssom.Color
import web.cssom.Display
import web.cssom.px

const val ESTIMATED_PLAYER_WIDTH = 40.0

val PairTickMark = FC<AxisTickProps> { props ->
    val getColor = useContext(colorContext)

    val pair = props.value.unsafeCast<CouplingPair>()
    val elementWidth = pair.count() * ESTIMATED_PLAYER_WIDTH
    val elementHeight = 45.0
    val backColor = getColor(props)
    g {
        transform = "translate(${props.x.toDouble()}, ${props.y.toDouble() + 22})"
        rect {
            x = -14.0
            y = -6.0
            ry = 3.0
            width = 28.0
            height = 24.0
            fill = "rgba(0, 0, 0, .05)"
        }
        line {
            stroke = backColor
            strokeWidth = 1.5
            y1 = -22.0
            y2 = -12.0
        }
        g {
            transform = "translate(${elementHeight / 2}, ${-24.0}) rotate(90)"
            foreignObject {
                width = elementWidth
                height = elementHeight
                div {
                    css {
                        display = Display.inlineBlock
                    }
                    div {
                        css {
                            backgroundColor = Color(backColor)
                            borderRadius = 5.px
                            paddingTop = 1.px
                            paddingBottom = 4.px
                        }
                        TiltedPlayerList(playerList = pair, size = 25)
                    }
                }
            }
        }
    }
}
