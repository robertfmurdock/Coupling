@file:JsModule("@nivo/heatmap")

package com.zegreatrob.coupling.client.components.external.nivo.heatmap

import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoDatum
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoHeatMapData
import react.ComponentType
import react.ElementType
import react.Props

external interface ResponsiveHeatMapProps : Props {
    var data: Array<NivoHeatMapData>
    var legend: String
    var colors: (NivoDatum) -> String
    var valueFormat: (Int) -> String
    var tooltip: ElementType<TooltipProps>
    var axisTop: NivoAxis
    var axisLeft: NivoAxis
    var axisRight: NivoAxis
    var axisBottom: NivoAxis
    var emptyColor: String
    var margin: NivoChartMargin
}

external val ResponsiveHeatMap: ComponentType<ResponsiveHeatMapProps>

external interface TooltipProps : Props {
    var cell: ComputedCell<out HeatMapDatum>
}

external interface HeatMapDatum {
    var x: Any
    var y: Any?
}

external interface ComputedCell<D> {
    var id: String
    var serieId: String
    var value: Number?
    var formattedValue: String?
    var data: D
    var x: Number
    var y: Number
    var width: Number
    var height: Number
    var color: String
    var opacity: Number
    var borderColor: String
    var label: String
    var labelTextColor: String
}
