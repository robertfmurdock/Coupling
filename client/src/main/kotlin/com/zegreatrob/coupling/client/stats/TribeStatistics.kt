package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/TribeStatistics")

data class TribeStatistics(val queryResults: StatisticQueryResults) : DataProps<TribeStatistics> {
    override val component: TMFC<TribeStatistics> = tribeStatistics
}

val tribeStatistics = tmFC<TribeStatistics> { props ->
    val (tribe, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div {
        className = styles.className
        ConfigHeader {
            this.tribe = tribe
            +"Statistics"
        }
        div {
            div {
                className = styles["leftSection"]
                div {
                    child(TeamStatistics(
                        spinsUntilFullRotation = spinsUntilFullRotation,
                        activePlayerCount = players.size,
                        medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
                    ))
                }
                child(PairReportTable(tribe, pairReports))
            }
            child(PlayerHeatmap(tribe, players, heatmapData))
        }
    }
}
