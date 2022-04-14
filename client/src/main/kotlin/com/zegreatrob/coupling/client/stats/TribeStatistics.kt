package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.html.classes
import react.dom.html.ReactHTML.div

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/TribeStatistics")

data class TribeStatistics(val queryResults: StatisticQueryResults) : DataPropsBind<TribeStatistics>(tribeStatistics)

val tribeStatistics = tmFC<TribeStatistics> { props ->
    val (tribe, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div {
        className = styles.className
        child(PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9"))) {
            ConfigHeader {
                this.party = tribe
                +"Statistics"
            }
            cssDiv(css = {
                whiteSpace = WhiteSpace.nowrap
                display = Display.inlineFlex
            }) {
                cssDiv(css = { flexGrow = 0.0 }, attrs = { classes = setOf("${styles["leftSection"]}") }) {
                    div {
                        child(TeamStatistics(
                            spinsUntilFullRotation = spinsUntilFullRotation,
                            activePlayerCount = players.size,
                            medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
                        ))
                    }
                    child(PairReportTable(pairReports))
                }
                child(PlayerHeatmap(players, heatmapData))
            }
        }
    }
}
