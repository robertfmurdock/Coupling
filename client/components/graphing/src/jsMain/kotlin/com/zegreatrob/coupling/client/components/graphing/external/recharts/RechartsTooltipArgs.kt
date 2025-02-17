package com.zegreatrob.coupling.client.components.graphing.external.recharts

import com.zegreatrob.coupling.client.components.graphing.external.nivo.RechartsTooltipPayload
import js.array.ReadonlyArray
import react.ReactNode

sealed external interface RechartsTooltipArgs {
    var label: Any?
    var labelFormatter: (label: Any?) -> ReactNode
    var payload: ReadonlyArray<RechartsTooltipPayload>?
}
