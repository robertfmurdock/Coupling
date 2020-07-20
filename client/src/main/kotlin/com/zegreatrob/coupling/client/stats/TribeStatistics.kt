package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.minreact.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import react.RProps
import react.dom.div


@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

private val styles = useStyles("stats/TribeStatistics")

data class TribeStatisticsProps(
    val queryResults: StatisticQueryResults,
    val pathSetter: (String) -> Unit
) : RProps

val TribeStatistics =
    reactFunction<TribeStatisticsProps> { props ->
        val (tribe, players, _, allStats, heatmapData) = props.queryResults
        val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
        div(classes = styles.className) {
            div {
                tribeCard(TribeCardProps(tribe, pathSetter = props.pathSetter))
                teamStatistics(
                    TeamStatisticsProps(
                        spinsUntilFullRotation = spinsUntilFullRotation,
                        activePlayerCount = players.size,
                        medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
                    )
                )
            }
            div {
                div(classes = styles["leftSection"]) {
                    pairReportTable(PairReportTableProps(tribe, pairReports))
                }

                playerHeatmap(PlayerHeatmapProps(tribe, players, heatmapData))
            }
        }
    }
