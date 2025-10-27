@file:JsModule("@nivo/heatmap")

package com.zegreatrob.coupling.client.components.external.nivo.heatmap

import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoDatum
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoHeatMapData
import kotlinx.js.JsPlainObject
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

@JsPlainObject
external interface HeatMapDatum {
    val x: Any
    val y: Any?
}

@JsPlainObject
external interface ComputedCell<D> {
    val id: String
    val serieId: String
    val value: Number?
    val formattedValue: String?
    val data: D
    val x: Number
    val y: Number
    val width: Number
    val height: Number
    val color: String
    val opacity: Number
    val borderColor: String
    val label: String
    val labelTextColor: String
}
