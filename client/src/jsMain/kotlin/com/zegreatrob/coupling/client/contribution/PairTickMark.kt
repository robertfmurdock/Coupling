package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.external.nivo.AxisTickProps
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
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
import web.cssom.Color
import web.cssom.Display
import web.cssom.px
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val ESTIMATED_PLAYER_WIDTH = 40.0

val PairTickMark = FC<AxisTickProps> { props ->
    val getColor = useContext(colorContext)
    val pairs = useContext(pairContext)
    val pair = pairs.find { it.pairId == props.value } ?: return@FC
    val backColor = getColor(props)
    val targetRef = useRef<web.html.HTMLDivElement>()
    val (elementWidth, setElementWidth) = useState(0)
    val (elementHeight, setElementHeight) = useState(0)

    useLayoutEffect(dependencies = emptyArray()) {
        targetRef.current?.let {
            setElementWidth(it.offsetWidth)
            setElementHeight(it.offsetHeight)
        }
    }

    val tickLength = 12.0
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
            y2 = -tickLength
        }

        val rotation = 90.0 + (props.rotate?.toDouble() ?: 0.0)

        val rotationRadians = rotation * PI / 180.0
//        val rotatedWidth = abs(elementWidth * sin(rotationRadians)) + abs(elementHeight * cos(rotationRadians))
        val rotatedHeight = abs(elementWidth * cos(rotationRadians)) + abs(elementHeight * sin(rotationRadians))
        g {
            transform = "translate(${rotatedHeight / 2}, ${-tickLength})"
            foreignObject {
                width = elementWidth.toDouble()
                height = elementHeight.toDouble()
                transform = "rotate($rotation)"
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
                        TiltedPlayerList(playerList = pair, size = 25)
                    }
                }
            }
        }
    }
}
