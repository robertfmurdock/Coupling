@file:JsModule("@nivo/tooltip")

package com.zegreatrob.coupling.client.components.external.nivo.tooltip

import react.ElementType
import react.Props
import react.ReactNode

external val BasicTooltip: ElementType<BasicTooltipProps>

external interface BasicTooltipProps : Props {
    var id: ReactNode
    var value: Any?
    var color: String?
    var enableChip: Boolean?
}
