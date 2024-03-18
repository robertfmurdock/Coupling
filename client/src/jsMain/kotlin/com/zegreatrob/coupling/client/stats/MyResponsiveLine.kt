package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.stats.GraphProps
import react.FC

val MyResponsiveLine = kotlinext.js.require<dynamic>("com/zegreatrob/coupling/client/ResponsiveLine.jsx")
    .MyResponsiveLine.unsafeCast<FC<GraphProps>>()
