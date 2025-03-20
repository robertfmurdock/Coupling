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
import com.zegreatrob.coupling.client.components.graphing.external.recharts.RechartsTooltipArgs
import com.zegreatrob.coupling.client.components.graphing.external.recharts.ResponsiveContainer
import com.zegreatrob.coupling.client.components.graphing.external.recharts.Tooltip
import com.zegreatrob.coupling.client.components.graphing.external.recharts.XAxis
import com.zegreatrob.coupling.client.components.graphing.external.recharts.YAxis
import js.lazy.Lazy
import js.objects.Object
import js.objects.Record
import js.objects.jso
import react.FC
import react.Props
import react.ReactNode
import react.useState
import web.cssom.WhiteSpace
import kotlin.js.Date

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: ((RechartsTooltipArgs) -> ReactNode)?
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
    val xValues = flattenedPoints.map { it.x }
    val xMaxMillis = xValues.max()
    val xMinMillis = xValues.min()
    val lineIds = props.data.map(NivoLineData::id).toTypedArray()
    val myColor = scaleOrdinal().domain(lineIds).range(schemeCategory10)
    val timeScale = scaleTime().domain(arrayOf(xMinMillis, xMaxMillis)).nice()
    val timeFormatter = timeFormat(calculatePrecision(xMaxMillis, xMinMillis))

    ResponsiveContainer {
        width = "100%"
        height = "100%"
        LineChart {
            margin = jso {
                bottom = 60
                left = 40
                right = 80
                top = 20
            }
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
                labelFormatter = timeFormatter
                content = props.tooltip
            }
            Legend {
                width = "90%"
                height = "7%"
                wrapperStyle = jso { whiteSpace = WhiteSpace.preWrap }
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

private fun calculatePrecision(min: Int, max: Int): String {
    val range = max - min
    val hasMinutes = (range / (1000 * 60)) > 1
    val hasHours = (range / (1000 * 60 * 60)) > 1
    val hasDays = (range / (1000 * 60 * 60 * 24)) > 1
    val hasMonths = (range / (1000 * 60 * 60 * 24 * 30)) > 1

    return when {
        hasMonths -> "%y-%m-%d"
        hasDays -> "%m-%d"
        hasHours -> "%H:%M"
        hasMinutes -> "%H:%M:%S"
        else -> "%H:%M:%S.%L"
    }
}

private fun Array<NivoLineData>.translateToLineChart(): Array<LinePoint> {
    val allPoints: List<LinePoint> = flatMap { line ->
        line.data.map { point ->
            Object.assign(
                jso<LinePoint>(),
                point,
                Record {
                    this["x"] = (point.x as Date?)?.getTime()
                    this[line.id] = point.y
                },
            )
        }
    }.sortedWith(comparator = { a, b -> a.x - b.x })

    val flattenedPoints = allPoints.groupBy { point -> point.x }
        .map { group ->
            group.value.reduce { result, current ->
                Object.assign(result, current)
            }
        }
    return flattenedPoints.toTypedArray()
}
