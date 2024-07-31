@file:JsModule("@nivo/boxplot")

package com.zegreatrob.coupling.client.components.external.nivo.boxplot

import com.zegreatrob.coupling.client.components.external.nivo.ComputedDatum
import com.zegreatrob.coupling.client.components.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import react.ComponentType
import react.Props

external interface ResponsiveBoxPlotProps : Props {
    var data: Array<dynamic>
    var minValue: Number
    var maxValue: Number
    var width: Number
    var height: Number
    var groupBy: String?
    var groups: Array<String>?
    var subGroupBy: String?
    var subGroups: Array<String>?
    var layout: String?
    var margin: NivoChartMargin?
    var padding: Number?
    var innerPadding: Number?
    var axisTop: NivoAxis?
    var axisLeft: NivoAxis?
    var axisRight: NivoAxis?
    var axisBottom: NivoAxis?
    var colors: NivoOrdinalScaleColorConfig?
    var valueFormat: (Number) -> String
    var tooltipLabel: ((ComputedDatum) -> String)?
}

external val ResponsiveBoxPlot: ComponentType<ResponsiveBoxPlotProps>
