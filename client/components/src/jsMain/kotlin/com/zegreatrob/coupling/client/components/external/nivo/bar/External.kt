@file:JsModule("@nivo/bar")

package com.zegreatrob.coupling.client.components.external.nivo.bar

import com.zegreatrob.coupling.client.components.external.nivo.ComputedDatum
import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import react.ComponentType
import react.Props
import kotlin.js.Json

external interface ResponsiveBarProps : Props {
    var data: Array<Json>
    var keys: Array<String>?
    var indexBy: String?
    var reverse: Boolean?
    var valueFormat: (Number) -> String
    var padding: Number?
    var innerPadding: Number?
    var width: Number?
    var height: Number?
    var margin: NivoChartMargin?
    var axisTop: NivoAxis?
    var axisLeft: NivoAxis?
    var axisRight: NivoAxis?
    var axisBottom: NivoAxis?
    var layout: String?
    var labelPosition: String?
    var labelOffset: Number?
    var groupMode: String?
    var colors: NivoOrdinalScaleColorConfig?
    var colorBy: String?
    var tooltipLabel: ((ComputedDatum) -> String)?
}

external val ResponsiveBar: ComponentType<ResponsiveBarProps>
