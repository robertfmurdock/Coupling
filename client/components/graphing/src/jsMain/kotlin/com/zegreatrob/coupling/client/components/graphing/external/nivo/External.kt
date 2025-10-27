package com.zegreatrob.coupling.client.components.graphing.external.nivo

import kotlinx.js.JsPlainObject
import react.ElementType
import react.Props

@JsPlainObject
sealed external interface NivoLineData {
    val id: String
    val data: Array<NivoPoint>
    val color: String?
}

@JsPlainObject
sealed external interface NivoPoint {
    val x: Any?
    val y: Any?
    val context: Any?
}

@JsPlainObject
sealed external interface NinoLinePointDecorated {
    val x: Any
    val xFormatted: Any
    val y: Any
    val yFormatted: Any
    val context: Any?
}

@JsPlainObject
external interface RechartsTooltipPayload {
    val name: Any?
    val value: Any?
}

@JsPlainObject
sealed external interface NivoChartMargin {
    val top: Number
    val right: Number
    val bottom: Number
    val left: Number
}

@JsPlainObject
sealed external interface NivoDatum {
    val id: String
    val value: Number
}

@JsPlainObject
sealed external interface NivoHeatMapData {
    val id: Any
    val data: Array<NivoPoint>
}

@JsPlainObject
sealed external interface NivoHeatMapColors {
    val type: String
    val scheme: String
    val divergeAt: Number
    val minValue: Number
    val maxValue: Number
}

@JsPlainObject
sealed external interface NivoAxis {
    val tickSize: Number?
    val tickPadding: Number?
    val tickRotation: Number?
    val ticksPosition: String?
    val legend: String?
    val legendPosition: String?
    val legendOffset: Number?
    val truncateTickAt: Number?
    val renderTick: ElementType<AxisTickProps>?
    val format: ((Number) -> String)?
}

external interface AxisTickProps : Props {
    var tickIndex: Number
    var value: String
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
