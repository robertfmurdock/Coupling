package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.graphing.external.nivo.AxisTickProps
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.player.Player
import emotion.react.css
import react.FC
import react.dom.html.ReactHTML.div
import react.dom.svg.ReactSVG.foreignObject
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.rect
import react.useContext
import react.useLayoutEffect
import react.useRef
import react.useState
import web.cssom.Angle
import web.cssom.Color
import web.cssom.Display
import web.cssom.px

const val ESTIMATED_PLAYER_WIDTH = 40.0

val PairTickMark = FC<AxisTickProps> { props ->
    val shouldInvert = props.textAnchor == "end"
    val inversionMultiplier = if (shouldInvert) -1 else 1
    val rotate = if (shouldInvert) {
        (props.rotate?.toDouble() ?: 0.0) + 180
    } else {
        props.rotate
    }
    val getColor = useContext(colorContext)
    val pairs = useContext(pairContext)
    val pair = pairs.find { it.pairId == props.value } ?: return@FC
    val backColor = getColor(props)
    val targetRef = useRef<web.html.HTMLDivElement>()
    val (elementWidth, setElementWidth) = useState(0)
    val (elementHeight, setElementHeight) = useState(0)

    useLayoutEffect {
        targetRef.current?.let {
            setElementWidth(it.offsetWidth)
            setElementHeight(it.offsetHeight)
        }
    }

    g {
        transform = "translate(${props.x.toDouble()}, ${props.y.toDouble()}) rotate($rotate)"
        rect {
            width = 28.0
            height = 24.0
            x = -(width!! / 2)
            y = if (shouldInvert) (height!! * 3 / -4) else (height!! / -4)
            ry = 3.0
            fill = "rgba(0, 0, 0, .05)"
        }
        val tickHeight = 10.0
        g {
            line {
                stroke = backColor
                strokeWidth = 1.5
                y1 = 0.0
                y2 = tickHeight * inversionMultiplier
            }
        }
        g {
            transform = "translate(0, ${tickHeight * inversionMultiplier})"
            g {
                val cardOffset = if (shouldInvert) -elementWidth else 0
                transform = "translate(${elementHeight / 2.0}, $cardOffset)"
                foreignObject {
                    width = elementWidth.toDouble()
                    height = elementHeight.toDouble()
                    transform = "rotate(90)"
                    div {
                        ref = targetRef
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
                            TiltedPlayerList(playerList = pair) { tilt: Angle, player: Player ->
                                PlayerCard(player, tilt = tilt, size = 25, key = player.id)
                            }
                        }
                    }
                }
            }
        }
    }
}
