package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.external.nivo.NinoLinePointDecorated
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.external.nivo.NivoLineData
import react.FC
import react.Props
import react.ReactNode

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: (NinoLinePointDecorated) -> ReactNode
    var xMin: kotlin.js.Date
    var xMax: kotlin.js.Date
}

val CouplingResponsiveLine = kotlinext.js.require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
    .CouplingResponsiveLine.unsafeCast<FC<CouplingResponsiveLineProps>>()

external interface CouplingResponsiveHeatMapProps : Props {
    var data: Array<NivoHeatMapData>
    var legend: String
    var colors: (NivoDatum) -> String
    var valueFormat: (Int) -> String
    var axisTop: NivoHeatMapAxis
    var axisLeft: NivoHeatMapAxis
    var axisRight: NivoHeatMapAxis
    var emptyColor: String
}

sealed external interface NivoDatum {
    var id: String
    var value: Number
}

val CouplingResponsiveHeatMap =
    kotlinext.js.require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
        .CouplingResponsiveHeatMap.unsafeCast<FC<CouplingResponsiveHeatMapProps>>()
