package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.stats.toValue
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.create
import react.useMemo
import kotlin.time.Clock
import kotlin.time.toJSDate

external interface AllEaseLineGraphProps : Props {
    var data: List<Contribution>
    var window: ContributionWindow
}

@ReactFunc
val AllEaseLineGraph by nfc<AllEaseLineGraphProps> { (data, window) ->
    val duration = window.toValue()
    val points = useMemo(data) {
        data.groupBy(contributionsByDate).mapNotNull(::dateContributionGroupToAverageEasePoint)
    }
    if (points.isNotEmpty()) {
        CouplingResponsiveLine {
            asDynamic()["data-testid"] = "all-ease-line-graph"
            legend = "All Ease Over Time"
            this.data = arrayOf(
                NivoLineData(
                    id = "All",
                    data = points.toTypedArray(),
                ),
            )
            yAxisDomain = arrayOf(0, 5)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    } else {
        +"No ease data available for this period."
    }
}
