package com.zegreatrob.coupling.client.components.external.nivo

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
    var id: String
    var data: Array<NivoPoint>
}

sealed external interface NivoHeatMapColors {
    var type: String
    var scheme: String
    var divergeAt: Number
    var minValue: Number
    var maxValue: Number
}

sealed external interface NivoHeatMapAxis {
    var tickSize: Number
    var tickPadding: Number
    var tickRotation: Number
    var legend: String?
    var legendPosition: String?
    var legendOffset: Number
    var truncateTickAt: Number
}
