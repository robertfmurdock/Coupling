package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.ComponentBuilder
import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.styledComponent
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.TribeCardRenderer
import com.zegreatrob.coupling.client.useState
import com.zegreatrob.coupling.common.ComposeStatisticsAction
import com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher
import com.zegreatrob.coupling.common.StatisticsReport
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder
import react.RProps
import react.dom.div


@JsModule("date-fns/distance_in_words")
@JsNonModule
external val distanceInWorks: (Int, Int?) -> String


interface TribeStatisticsSyntax {
    companion object : TribeStatisticsBuilder {
        val component = build()
    }

    fun RBuilder.tribeStatistics(props: TribeStatisticsProps) = component(component, props)
}

interface TribeStatisticsBuilder : ComponentBuilder<TribeStatisticsProps>,
        TribeCardRenderer, TeamStatisticsSyntax, PairReportTableSyntax,
        PlayerHeatmapSyntax, ComposeStatisticsActionDispatcher, CalculateHeatMapCommandDispatcher {

    override fun build() = styledComponent("stats/TribeStatistics")
    { props: TribeStatisticsProps, styles: TribeStatisticsStyles ->
        val (tribe, players, history) = props

        val (allStats) = useState { calculateStats(tribe, players, history) }

        val (spinsUntilFullRotation, pairReports, medianSpinDuration) = allStats.first
        val heatmapData = allStats.second

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

    private fun calculateStats(
            tribe: KtTribe,
            players: List<Player>,
            history: List<PairAssignmentDocument>
    ): Pair<StatisticsReport, List<List<Double?>>> {
        val stats = ComposeStatisticsAction(tribe, players, history).perform()
        return stats to
                CalculateHeatMapCommand(players, history, stats.spinsUntilFullRotation)
                        .perform()
    }

}

external interface TribeStatisticsStyles {
    val className: String
    val leftSection: String
}

data class TribeStatisticsProps(
        val tribe: KtTribe,
        val players: List<Player>,
        val history: List<PairAssignmentDocument>,
        val pathSetter: (String) -> Unit
) : RProps
