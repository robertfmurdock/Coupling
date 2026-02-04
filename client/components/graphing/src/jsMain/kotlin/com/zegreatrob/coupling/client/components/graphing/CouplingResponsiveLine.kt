package com.zegreatrob.coupling.client.components.graphing

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.chromatic.schemeCategory10
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleOrdinal
import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.scaleTime
import com.zegreatrob.coupling.client.components.graphing.external.d3.timeformat.timeFormat
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.graphing.external.recharts.CartesianGrid
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Legend
import com.zegreatrob.coupling.client.components.graphing.external.recharts.LegendEvent
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Line
import com.zegreatrob.coupling.client.components.graphing.external.recharts.LineChart
import com.zegreatrob.coupling.client.components.graphing.external.recharts.LinePoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.RechartsMargin
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Tooltip
import com.zegreatrob.coupling.client.components.graphing.external.recharts.TooltipProps
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import emotion.react.css
import js.lazy.Lazy
import js.objects.Object
import js.objects.recordOf
import js.objects.unsafeJso
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.WhiteSpace
import web.cssom.pct
import kotlin.js.Date
import kotlin.math.max

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: ((TooltipProps) -> ReactNode)?
    var xMin: Date
    var xMax: Date
    var yAxisDomain: Array<Any>?
}

@Lazy
val CouplingResponsiveLine = FC<CouplingResponsiveLineProps> { props ->
    val (activeSeries, setActiveSeries) = useState(emptyList<String>())
    val onLegendClick = fun(event: LegendEvent) {
        if (activeSeries.contains(event.dataKey)) {
            setActiveSeries { prev -> prev.filter { it != event.dataKey } }
        } else {
            setActiveSeries { prev -> prev + event.dataKey }
        }
    }
    val flattenedPoints = props.data.translateToLineChart()
    val xValues = flattenedPoints.map { it.x.unsafeCast<Double>() }
    val xMaxMillis = xValues.maxOrNull()
    val xMinMillis = xValues.minOrNull()
    val lineIds = props.data.map(NivoLineData::id).toTypedArray()
    val myColor = scaleOrdinal().domain(lineIds).range(schemeCategory10)
    div {
        css {
            width = 100.pct
            height = 100.pct
        }
        asDynamic()["data-testid"] = "coupling-responsive-line"
        if (xMaxMillis == null || xMinMillis == null) {
            return@div
        }

        val timeScale = scaleTime().domain(arrayOf(xMinMillis, xMaxMillis)).nice()
        val timeFormatter = timeFormat(scaledTimeFormat(xMinMillis, xMaxMillis))
        ResponsiveContainer {
            width = "100%"
            height = "100%"
            LineChart {
                margin = RechartsMargin(bottom = 60, left = 40, right = 80, top = 20)
                data = flattenedPoints
                CartesianGrid {
                    strokeDasharray = "3 3"
                }
                XAxis {
                    dataKey = "x"
                    domain = timeScale.domain().map(js.date.Date::valueOf).toTypedArray()
                    scale = timeScale
                    type = "number"
                    ticks = timeScale.ticks(5).map(js.date.Date::valueOf).toTypedArray()
                    tickFormatter = timeFormatter
                    fontSize = 12
                }
                YAxis {
                    type = "number"
                    dataKey = "y"
                    domain = props.yAxisDomain
                }
                Tooltip {
                    labelFormatter = { ReactNode(timeFormatter(it)) }
                    content = props.tooltip
                }
                Legend {
                    width = "90%"
                    height = "7%"
                    wrapperStyle = unsafeJso { whiteSpace = WhiteSpace.preWrap }
                    onClick = onLegendClick
                }
                lineIds.forEachIndexed { index, lineId ->
                    Line {
                        connectNulls = true
                        key = lineId
                        type = "monotone"
                        dataKey = lineId
                        hide = activeSeries.contains(lineId)
                        stroke = myColor(index)
                        labelFormatter = timeFormatter
                    }
                }
            }
        }
    }
}

fun Array<NivoLineData>.translateToLineChart(): Array<LinePoint> {
    val allPoints: List<LinePoint> = flatMap { line ->
        line.data.map { point ->
            Object.assign(
                unsafeJso<LinePoint>(),
                point,
                recordOf(
                    "x" to (point.x as Date?)?.getTime(),
                    line.id to point.y,
                ),
            )
        }
    }.sortedBy { it.x.unsafeCast<Double>() }

    val flattenedPoints = allPoints.groupBy { point -> point.x }
        .map { group ->
            group.value.reduce { result, current ->
                Object.assign(
                    result,
                    current,
                    recordOf("y" to max(result.y, current.y)),
                )
            }
        }
    return flattenedPoints.toTypedArray()
}
