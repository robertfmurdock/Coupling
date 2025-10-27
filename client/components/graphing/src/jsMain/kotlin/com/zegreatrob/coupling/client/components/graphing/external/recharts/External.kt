@file:JsModule("recharts")

package com.zegreatrob.coupling.client.components.graphing.external.recharts

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.TimeScale
import com.zegreatrob.coupling.client.components.graphing.external.nivo.RechartsTooltipPayload
import js.array.ReadonlyArray
import kotlinx.js.JsPlainObject
import react.FC
import react.Props
import react.PropsWithChildren
import react.ReactNode

external interface ResponsiveContainerProps : PropsWithChildren {
    var width: String
    var height: String
}

external val ResponsiveContainer: FC<ResponsiveContainerProps>

@JsPlainObject
external interface RechartsMargin {
    val top: Number
    val left: Number
    val right: Number
    val bottom: Number
}

@JsPlainObject
external interface LinePoint {
    val x: Any
    val y: Double
    val z: Any?
}

external interface LineChartProps : PropsWithChildren {
    var data: Array<LinePoint>
    var margin: RechartsMargin
}

external val LineChart: FC<LineChartProps>

external interface ScatterChartProps : PropsWithChildren {
    var margin: RechartsMargin
}

external val ScatterChart: FC<ScatterChartProps>

external interface AreaChartProps : PropsWithChildren {
    var data: Array<LinePoint>
    var margin: RechartsMargin
}

external val AreaChart: FC<AreaChartProps>
external val BarChart: FC<AreaChartProps>
external val ComposedChart: FC<AreaChartProps>

external interface CartesianGridProps : Props {
    var strokeDasharray: String
}

external val CartesianGrid: FC<CartesianGridProps>

external interface LineProps : Props {
    var connectNulls: Boolean?
    var type: String
    var dataKey: String
    var hide: Boolean?
    var stroke: Any
    var labelFormatter: (value: Any) -> String
}

external val Line: FC<LineProps>

external interface ScatterProps : Props {
    var connectNulls: Boolean?
    var type: String
    var data: Array<LinePoint>
    var dataKey: String
    var hide: Boolean?
    var stroke: Any
    var labelFormatter: (value: Any) -> String
}

external val Scatter: FC<ScatterProps>

external interface TooltipProps : Props {
    var labelFormatter: (value: Any?) -> ReactNode
    var formatter: (value: Any?, name: Any?) -> ReactNode
    var content: ((TooltipProps) -> ReactNode)?
    var payload: ReadonlyArray<RechartsTooltipPayload>?
    var label: Any?
}

external val Tooltip: FC<TooltipProps>

external interface LegendProps : Props {
    var width: String
    var height: String
    var wrapperStyle: react.CSSProperties?
    var content: ((LegendProps) -> ReactNode)?
    var onClick: (LegendEvent) -> Unit
    var payload: ReadonlyArray<RechartsTooltipPayload>?
}

@JsPlainObject
external interface LegendEvent {
    val dataKey: String
}

external val Legend: FC<LegendProps>

external interface TickProps : Props {
    var x: Double
    var y: Double
    var payload: RechartsTooltipPayload
}

external interface XAxisProps : Props {
    var dataKey: String
    var domain: Array<*>
    var scale: TimeScale
    var type: String
    var name: String
    var interval: Any
    var ticks: Array<*>
    var tick: FC<TickProps>
    var tickFormatter: (value: Any) -> String
    var fontSize: Number
}

external val XAxis: FC<XAxisProps>

external interface YAxisProps : Props {
    var dataKey: String
    var name: String
    var type: String
    var domain: Array<Any>?
    var interval: Any
    var ticks: Array<Any>?
}

external val YAxis: FC<YAxisProps>

external interface ZAxisProps : Props {
    var dataKey: String
    var name: String
    var type: String
    var domain: Array<Any>?
    var range: Array<Any>?
}

external val ZAxis: FC<ZAxisProps>

external interface AreaProps : Props {
    var type: String
    var dataKey: String
    var stackId: String
    var stroke: Any
    var fill: Any
}

external val Area: FC<AreaProps>

external interface BarProps : Props {
    var type: String
    var barSize: Any
    var dataKey: String
    var stackId: String
    var stroke: Any
    var fill: Any
}

external val Bar: FC<BarProps>
