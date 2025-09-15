package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.create
import kotlin.time.Clock
import kotlin.time.toJSDate

external interface PairContributionsLineGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairContributionsLineGraph by nfc<PairContributionsLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    val lines = data.map {
        Pair(
            it.first.joinToString("-", transform = Player::name),
            (it.second.contributions?.elements ?: emptyList()).groupBy(contributionsByDate)
                .mapNotNull(::timeByContributionCountPoint),
        )
    }
    if (lines.flatMap { it.second }.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Pair Commits Over Time"
            this.data = lines
                .map { (first, second) -> NivoLineData(first, second.toTypedArray()) }
                .toTypedArray()

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
