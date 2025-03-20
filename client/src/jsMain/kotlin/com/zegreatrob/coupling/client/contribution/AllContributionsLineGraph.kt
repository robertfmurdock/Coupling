package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.datetime.Clock
import kotlinx.datetime.toJSDate
import react.Props
import react.create

external interface AllContributionsLineGraphProps : Props {
    var data: List<Contribution>
    var window: GqlContributionWindow
}

@ReactFunc
val AllContributionsLineGraph by nfc<AllContributionsLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Contributions Over Time"
            this.data = arrayOf(contributionLine(data))

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    }
}

private fun contributionLine(contributions: List<Contribution>) = NivoLineData(
    "All",
    contributions.groupBy(contributionsByDate)
        .mapNotNull(::timeByContributionCountPoint)
        .toTypedArray(),
)
