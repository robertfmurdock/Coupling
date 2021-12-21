package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.dom.div

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/TribeStatistics")

data class TribeStatistics(val queryResults: StatisticQueryResults) : DataProps<TribeStatistics> {
    override val component: TMFC<TribeStatistics> = tribeStatistics
}

val tribeStatistics = reactFunction<TribeStatistics> { props ->
    val (tribe, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div(classes = styles.className) {
        div {
            tribeCard(TribeCard(tribe))
            child(TeamStatistics(
                spinsUntilFullRotation = spinsUntilFullRotation,
                activePlayerCount = players.size,
                medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
            ))
        }
        div {
            div(classes = styles["leftSection"]) {
                child(PairReportTable(tribe, pairReports))
            }
            child(PlayerHeatmap(tribe, players, heatmapData))
        }
    }
}
