package com.zegreatrob.coupling.client.components.graphing.external.nivo

import react.ElementType
import react.Props

sealed external interface NivoLineData {
    var id: String
    var data: Array<NivoPoint>
    var color: String?
}

sealed external interface NivoPoint {
    var x: Any?
    var y: Any?
    var context: Any?
}

sealed external interface NinoLinePointDecorated {
    var x: Any
    var xFormatted: Any
    var y: Any
    var yFormatted: Any
    var context: Any?
}

external interface RechartsTooltipPayload {
    var name: Any?
    var value: Any?
}

sealed external interface NivoChartMargin {
    var top: Number
    var right: Number
    var bottom: Number
    var left: Number
}

sealed external interface NivoDatum {
    var id: String
    var value: Number
}

sealed external interface NivoHeatMapData {
    var id: Any
    var data: Array<NivoPoint>
}

sealed external interface NivoHeatMapColors {
    var type: String
    var scheme: String
    var divergeAt: Number
    var minValue: Number
    var maxValue: Number
}

sealed external interface NivoAxis {
    var tickSize: Number?
    var tickPadding: Number?
    var tickRotation: Number?
    var ticksPosition: String?
    var legend: String?
    var legendPosition: String?
    var legendOffset: Number?
    var truncateTickAt: Number?
    var renderTick: ElementType<AxisTickProps>?
    var format: ((Number) -> String)?
}

external interface AxisTickProps : Props {
    var tickIndex: Number
    var value: dynamic
    var format: ((Number) -> String)?
    var x: Number
    var y: Number
    var lineX: Number
    var lineY: Number
    var textX: Number
    var textY: Number
    var textBaseline: String
    var textAnchor: String
    var opacity: Number?
    var rotate: Number?
    var truncateTickAt: Number?
}
