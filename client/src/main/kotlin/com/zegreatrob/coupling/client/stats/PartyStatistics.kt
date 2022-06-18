package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.number
import emotion.react.css
import react.dom.html.ReactHTML.div

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/PartyStatistics")

data class PartyStatistics(val queryResults: StatisticQueryResults) : DataPropsBind<PartyStatistics>(partyStatistics)

val partyStatistics = tmFC<PartyStatistics> { props ->
    val (party, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div {
        className = styles.className
        +PageFrame(borderColor = csstype.Color("#e8e8e8"), backgroundColor = csstype.Color("#dcd9d9")).create {
            ConfigHeader {
                this.party = party
                +"Statistics"
            }
            div {
                css {
                    whiteSpace = csstype.WhiteSpace.nowrap
                    display = csstype.Display.inlineFlex
                }
                div {
                    css(styles["leftSection"]) {
                        flexGrow = number(0.0)
                    }
                    div {
                        +TeamStatistics(
                            spinsUntilFullRotation = spinsUntilFullRotation,
                            activePlayerCount = players.size,
                            medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) }
                                ?: ""
                        ).create()
                    }
                    +PairReportTable(pairReports).create()
                }
                +PlayerHeatmap(players, heatmapData).create()
            }
        }
    }
}
