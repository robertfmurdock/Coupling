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
import kotlin.time.Clock
import kotlin.time.toJSDate

external interface AllContributionsLineGraphProps : Props {
    var data: List<Contribution>
    var window: ContributionWindow
}

@ReactFunc
val AllContributionsLineGraph by nfc<AllContributionsLineGraphProps> { (data, window) ->
    val duration = window.toValue()
    val points = data.groupBy(contributionsByDate)
        .mapNotNull(::timeByContributionCountPoint)

    if (points.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Contributions Over Time"
            this.data = arrayOf(NivoLineData("All", points.toTypedArray()))

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    } else {
        +"No contributions with time data available for this period."
    }
}
