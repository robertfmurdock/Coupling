package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RProps
import react.dom.div


@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/TribeStatistics")

data class TribeStatisticsProps(
    val queryResults: StatisticQueryResults,
    val pathSetter: (String) -> Unit
) : RProps

val TribeStatistics = reactFunction<TribeStatisticsProps> { props ->
    val (tribe, players, _, allStats, heatmapData) = props.queryResults
    val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
    div(classes = styles.className) {
        div {
            tribeCard(TribeCardProps(tribe, pathSetter = props.pathSetter))
            child(TeamStatistics, TeamStatisticsProps(
                spinsUntilFullRotation = spinsUntilFullRotation,
                activePlayerCount = players.size,
                medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
            ))
        }
        div {
            div(classes = styles["leftSection"]) {
                child(PairReportTable, PairReportTableProps(tribe, pairReports))
            }
            playerHeatmap(PlayerHeatmapProps(tribe, players, heatmapData))
        }
    }
}
