package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
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

external interface PartyStatisticsProps : Props {
    var queryResults: StatisticsQuery.Results
}

@ReactFunc
val PartyStatistics by nfc<PartyStatisticsProps> { props ->
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
                        TeamStatistics(
                            spinsUntilFullRotation = spinsUntilFullRotation,
                            activePlayerCount = players.size,
                            medianSpinDuration = medianSpinDuration?.let {
                                formatDistance(medianSpinDuration.inWholeMilliseconds.toInt(), 0)
                            } ?: "",
                        )
                    }
                    PairReportTable(pairReports)
                }
                PlayerHeatmap(players, heatmapData)
            }
        }
    }
}
