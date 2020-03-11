package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.action.entity.heatmap.CalculateHeatMapActionDispatcher
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.tribeCard
import react.RProps
import react.ReactElement
import react.dom.div


@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance = formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()

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
    CalculateHeatMapActionDispatcher,
    NullTraceIdProvider {

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
                            medianSpinDuration = medianSpinDuration?.let { formatDistance(it.millisecondsInt, 0) } ?: ""
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
