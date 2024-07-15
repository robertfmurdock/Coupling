package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapColors
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.external.nivo.NivoPoint
import com.zegreatrob.coupling.client.components.stats.CouplingResponsiveHeatMap
import com.zegreatrob.coupling.client.components.stats.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.stats.adjustDatasetForHeatMap
import com.zegreatrob.coupling.client.components.stats.toNivoHeatmapSettings
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.px

external interface PairFrequencyLineGraphProps : Props {
    var data: List<Pair<CouplingPair, List<Contribution>>>
    var window: JsonContributionWindow
}

@ReactFunc
val PairFrequencyLineGraph by nfc<PairFrequencyLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.flatMap { it.second }.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Pair Commits Over Time"
            this.data = pairingLineData(data)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()

            tooltip = { point ->
                div.create {
                    css {
                        backgroundColor = Color("rgb(0 0 0 / 14%)")
                        padding = 10.px
                        borderRadius = 20.px
                    }
                    div { +"${point.xFormatted} - ${point.yFormatted}" }
                    div { +"${point.context}" }
                }
            }
        }
    }
}

private fun pairingLineData(selectedPairs: List<Pair<CouplingPair, List<Contribution>>>): Array<NivoLineData> =
    selectedPairs.map { pairContributionLine(it.first, it.second) }.toTypedArray()

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>) =
    NivoLineData(
        couplingPair.joinToString("-") { it.name },
        contributions.groupBy { contribution ->
            contribution.dateTime
                ?.toLocalDateTime(TimeZone.currentSystemDefault())
                ?.date
        }.mapNotNull {
            val date = it.key ?: return@mapNotNull null
            NivoPoint(
                x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
                y = it.value.size,
                context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
            )
        }.toTypedArray(),
    )

external interface PairFrequencyHeatMapProps : Props {
    var data: List<Pair<CouplingPair, List<Contribution>>>
    var window: JsonContributionWindow
    var spinsUntilFullRotation: Int
}

@ReactFunc
val PairFrequencyHeatMap by nfc<PairFrequencyHeatMapProps> { (contributionData, window, spinsUntilFullRotation) ->
    val inclusiveContributions = adjustDatasetForHeatMap(contributionData.toMap())
    val (max, data: Array<NivoHeatMapData>) = inclusiveContributions.toNivoHeatmapSettings(window, spinsUntilFullRotation)
    CouplingResponsiveHeatMap {
        legend = "Pair Commits"
        this.data = data
        colors = NivoHeatMapColors(
            type = "diverging",
            scheme = "red_yellow_blue",
            divergeAt = 0.6,
            minValue = 0,
            maxValue = max,
        )
        valueFormat = { y -> "${max - y}" }
        axisRight = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = 0,
            legend = "Player",
            legendPosition = "middle",
            legendOffset = 70,
            truncateTickAt = 0,
        )
        axisLeft = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = 0,
            legend = "Player",
            legendPosition = "middle",
            legendOffset = -72,
            truncateTickAt = 0,
        )
        axisTop = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = -90,
            legend = "",
            legendOffset = 46,
            truncateTickAt = 0,
        )
    }
}
