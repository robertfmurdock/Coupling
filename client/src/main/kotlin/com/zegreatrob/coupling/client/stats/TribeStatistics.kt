package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ComponentProvider
import com.zegreatrob.coupling.client.StyledComponentBuilder
import com.zegreatrob.coupling.client.buildBy
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import react.RProps
import react.dom.div


@JsModule("date-fns/distance_in_words")
@JsNonModule
external val distanceInWorks: (Int, Int?) -> String

object TribeStatistics : ComponentProvider<TribeStatisticsProps>(), TribeStatisticsBuilder


external interface TribeStatisticsStyles {
    val className: String
    val leftSection: String
}

data class TribeStatisticsProps(
        val queryResults: StatisticQueryResults,
        val pathSetter: (String) -> Unit
) : RProps

interface TribeStatisticsBuilder : StyledComponentBuilder<TribeStatisticsProps, TribeStatisticsStyles>,
        ComposeStatisticsActionDispatcher,
        CalculateHeatMapCommandDispatcher {

    override val componentPath: String get() = "stats/TribeStatistics"

    override fun build() = buildBy {
        val (tribe, players, _, allStats, heatmapData) = props.queryResults
        val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
        {
            div(classes = styles.className) {
                div {
                    tribeCard(TribeCardProps(tribe, pathSetter = props.pathSetter))
                    teamStatistics(TeamStatisticsProps(
                            spinsUntilFullRotation = spinsUntilFullRotation,
                            activePlayerCount = players.size,
                            medianSpinDuration = distanceInWorks(0, medianSpinDuration?.millisecondsInt)
                    ))
                }
                div {
                    div(classes = styles.leftSection) {
                        pairReportTable(PairReportTableProps(tribe, pairReports))
                    }

                    playerHeatmap(PlayerHeatmapProps(tribe, players, heatmapData))
                }
            }
        }
    }
}
