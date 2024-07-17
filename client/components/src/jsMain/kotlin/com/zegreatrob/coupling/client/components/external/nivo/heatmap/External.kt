@file:JsModule("@nivo/heatmap")

package com.zegreatrob.coupling.client.components.external.nivo.heatmap

import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.NivoDatum
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import react.ComponentType
import react.Props

external interface ResponsiveHeatMapProps : Props {
    var data: Array<NivoHeatMapData>
    var legend: String
    var colors: (NivoDatum) -> String
    var valueFormat: (Int) -> String
    var axisTop: NivoHeatMapAxis
    var axisLeft: NivoHeatMapAxis
    var axisRight: NivoHeatMapAxis
    var emptyColor: String
    var margin: NivoChartMargin
}

external val ResponsiveHeatMap: ComponentType<ResponsiveHeatMapProps>
