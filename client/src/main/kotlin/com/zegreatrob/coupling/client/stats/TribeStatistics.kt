package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapActionDispatcher
import react.RProps
import react.ReactElement
import react.dom.div


@JsModule("date-fns/distance_in_words")

external val distanceInWorks: (Int, Int?) -> String

object TribeStatistics : RComponent<TribeStatisticsProps>(provider()), TribeStatisticsBuilder

external interface TribeStatisticsStyles {
    val className: String
    val leftSection: String
}

data class TribeStatisticsProps(
    val queryResults: StatisticQueryResults,
    val pathSetter: (String) -> Unit
) : RProps

interface TribeStatisticsBuilder : StyledComponentRenderer<TribeStatisticsProps, TribeStatisticsStyles>,
    ComposeStatisticsActionDispatcher,
    CalculateHeatMapActionDispatcher {

    override val componentPath: String get() = "stats/TribeStatistics"

    override fun StyledRContext<TribeStatisticsProps, TribeStatisticsStyles>.render(): ReactElement {
        val (tribe, players, _, allStats, heatmapData) = props.queryResults
        val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats
        return reactElement {
            div(classes = styles.className) {
                div {
                    tribeCard(TribeCardProps(tribe, pathSetter = props.pathSetter))
                    teamStatistics(
                        TeamStatisticsProps(
                            spinsUntilFullRotation = spinsUntilFullRotation,
                            activePlayerCount = players.size,
                            medianSpinDuration = distanceInWorks(0, medianSpinDuration?.millisecondsInt)
                        )
                    )
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
