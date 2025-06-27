package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.create
import kotlin.time.Clock
import kotlin.time.toJSDate

external interface AllEaseLineGraphProps : Props {
    var data: List<Contribution>
    var window: GqlContributionWindow
}

@ReactFunc
val AllEaseLineGraph by nfc<AllEaseLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "All Ease Over Time"
            this.data = easeLineData(data)
            yAxisDomain = arrayOf(0, 5)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    }
}

private fun easeLineData(contributions: List<Contribution>): Array<NivoLineData> = arrayOf(
    pairContributionLine(contributions),
)

private fun pairContributionLine(contributions: List<Contribution>) = NivoLineData(
    id = "All",
    data = contributions.groupBy(contributionsByDate)
        .mapNotNull(::dateContributionGroupToAverageEasePoint)
        .toTypedArray(),
)
