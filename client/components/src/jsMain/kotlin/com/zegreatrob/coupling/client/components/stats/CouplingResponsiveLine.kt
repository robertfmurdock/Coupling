package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.external.nivo.RechartsTooltipArgs
import com.zegreatrob.coupling.client.components.require
import react.FC
import react.Props
import react.ReactNode

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: (RechartsTooltipArgs) -> ReactNode
    var xMin: kotlin.js.Date
    var xMax: kotlin.js.Date
}

val CouplingResponsiveLine = require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
    .CouplingResponsiveLine.unsafeCast<FC<CouplingResponsiveLineProps>>()
