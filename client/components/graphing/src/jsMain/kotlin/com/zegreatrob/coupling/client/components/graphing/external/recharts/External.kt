@file:JsModule("recharts")

package com.zegreatrob.coupling.client.components.graphing.external.recharts

import com.zegreatrob.coupling.client.components.graphing.external.d3.scale.TimeScale
import react.FC
import react.Props
import react.PropsWithChildren
import react.ReactNode

external interface ResponsiveContainerProps : PropsWithChildren {
    var width: String
    var height: String
}

external val ResponsiveContainer: FC<ResponsiveContainerProps>

external interface RechartsMargin {
    var top: Number
    var left: Number
    var right: Number
    var bottom: Number
}

external interface LinePoint {
    var x: Long
}

external interface LineChartProps : PropsWithChildren {
    var data: Array<LinePoint>
    var margin: RechartsMargin
}

external val LineChart: FC<LineChartProps>

external interface AreaChartProps : PropsWithChildren {
    var data: Array<LinePoint>
    var margin: RechartsMargin
}
external val AreaChart: FC<AreaChartProps>

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

external interface TooltipProps : Props {
    var labelFormatter: (value: Any) -> String
    var content: ((RechartsTooltipArgs) -> ReactNode)?
}

external val Tooltip: FC<TooltipProps>

external interface LegendProps : Props {
    var width: String
    var height: String
    var wrapperStyle: react.CSSProperties?
    var onClick: (LegendEvent) -> Unit
}

external interface LegendEvent {
    var dataKey: String
}

external val Legend: FC<LegendProps>

external interface XAxisProps : Props {
    var dataKey: String
    var domain: Array<*>
    var scale: TimeScale
    var type: String
    var ticks: Array<*>
    var tickFormatter: (value: Any) -> String
    var fontSize: Number
}

external val XAxis: FC<XAxisProps>

external interface YAxisProps : Props {
    var dataKey: String
    var type: String
    var domain: Array<Any>?
}

external val YAxis: FC<YAxisProps>

external interface AreaProps : Props {
    var type: String
    var dataKey: String
    var stackId: String
    var stroke: Any
    var fill: Any
}

external val Area: FC<AreaProps>
