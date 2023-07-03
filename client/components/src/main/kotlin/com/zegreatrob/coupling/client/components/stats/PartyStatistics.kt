package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.Display
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.number

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance: (Int?, Int) -> String = if (formatDistanceModule.default != undefined) {
    formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()
} else {
    formatDistanceModule.unsafeCast<(Int?, Int) -> String>()
}

data class PartyStatistics(val queryResults: StatisticsQuery.Results) : DataPropsBind<PartyStatistics>(
    partyStatistics,
)

val partyStatistics by ntmFC<PartyStatistics> { props ->
    val (party, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = party
                +"Statistics"
            }
            div {
                css {
                    whiteSpace = WhiteSpace.nowrap
                    display = Display.inlineFlex
                }
                div {
                    css {
                        display = Display.inlineBlock
                        verticalAlign = VerticalAlign.top
                        flexGrow = number(0.0)
                    }
                    div {
                        add(
                            TeamStatistics(
                                spinsUntilFullRotation = spinsUntilFullRotation,
                                activePlayerCount = players.size,
                                medianSpinDuration = medianSpinDuration?.let {
                                    formatDistance(medianSpinDuration.millisecondsInt, 0)
                                } ?: "",
                            ),
                        )
                    }
                    add(PairReportTable(pairReports))
                }
                add(PlayerHeatmap(players, heatmapData))
            }
        }
    }
}
