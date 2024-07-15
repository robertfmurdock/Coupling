package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.stats.CouplingResponsiveHeatMapProps
import com.zegreatrob.coupling.client.components.stats.CouplingResponsiveLineProps
import react.FC

val CouplingResponsiveLine = kotlinext.js.require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
    .CouplingResponsiveLine.unsafeCast<FC<CouplingResponsiveLineProps>>()

val CouplingResponsiveHeatMap = kotlinext.js.require<dynamic>("com/zegreatrob/coupling/client/CouplingResponsiveLine.jsx")
    .CouplingResponsiveHeatMap.unsafeCast<FC<CouplingResponsiveHeatMapProps>>()
