package com.zegreatrob.coupling.client.components.graphing

import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.graphing.external.nivo.RechartsTooltipArgs
import js.lazy.Lazy
import react.FC
import react.Props
import react.ReactNode
import kotlin.js.Date

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: (RechartsTooltipArgs) -> ReactNode
    var xMin: Date
    var xMax: Date
}

external fun <T> require(@Suppress("unused") module: String): T

@Lazy
val CouplingResponsiveLine = require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
    .CouplingResponsiveLine.unsafeCast<FC<CouplingResponsiveLineProps>>()
