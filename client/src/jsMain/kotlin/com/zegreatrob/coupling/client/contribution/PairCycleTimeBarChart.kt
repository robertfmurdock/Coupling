package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.external.nivo.AxisTickProps
import com.zegreatrob.coupling.client.components.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.bar.ResponsiveBar
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.svg.ReactSVG.foreignObject
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.line
import react.dom.svg.ReactSVG.rect
import web.cssom.Color
import web.cssom.Display
import web.cssom.px
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

external interface PairCycleTimeBarChartProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairCycleTimeBarChart by nfc<PairCycleTimeBarChartProps> { props ->
    val pairToCycleTime =
        props.data.mapNotNull { (pair, report) -> pair to (report.medianCycleTime ?: return@mapNotNull null) }

    val data = pairToCycleTime.map { (pair, cycleTime) ->
        jso<dynamic> {
            this.pair = pair
            value = cycleTime.inWholeMilliseconds
        }
    }.toTypedArray()
    val formatMillisAsDuration: (Number) -> String = { value ->
        (Duration.ZERO + value.toLong().milliseconds).toString()
    }
    val largestMobSize = pairToCycleTime.toMap().keys.maxOfOrNull { it.count() } ?: 2

    ResponsiveBar {
        this.data = data
        this.keys = arrayOf("value")
        this.indexBy = "pair"
        margin = NivoChartMargin(
            top = 65,
            right = 90,
            bottom = 10 + estimatedPlayerWidth * largestMobSize,
            left = 90,
        )
        this.valueFormat = formatMillisAsDuration
        this.axisLeft = NivoAxis(
            format = formatMillisAsDuration,
        )
        this.axisBottom = NivoAxis(renderTick = PairTickMark)
        this.layout = "vertical"
        this.labelPosition = "end"
        this.labelOffset = -10
        this.groupMode = "grouped"
        tooltipLabel = { data ->
            "${data.indexValue.unsafeCast<CouplingPair>().pairName} - ${data.formattedValue}"
        }
    }
}

val estimatedPlayerWidth = 40.0

val PairTickMark = FC<AxisTickProps> { props ->
    val pair = props.value.unsafeCast<CouplingPair>()
    val elementWidth = pair.count() * estimatedPlayerWidth
    val elementHeight = 45.0
    val backColor = "rgb(232, 193, 160)"
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
