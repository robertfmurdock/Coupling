package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.PairTickMark
import com.zegreatrob.coupling.client.components.colorContext
import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.ResponsiveHeatMap
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.graphing.interpolatorAsync
import com.zegreatrob.coupling.client.components.pairContext
import com.zegreatrob.coupling.client.components.stats.adjustDatasetForHeatMap
import com.zegreatrob.coupling.client.components.stats.toNivoHeatmapSettings
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.useEffect
import react.useState

external interface PairContributionsHeatMapProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: ContributionWindow
    var spinsUntilFullRotation: Int
}

@ReactFunc
val PairContributionsHeatMap by nfc<PairContributionsHeatMapProps> { (contributionData, window, spinsUntilFullRotation) ->
    val getColor = useOrdinalColorScale(NivoOrdinalScaleColorConfig(scheme = "pastel1"), "value")
    val (interpolator, setInterpolator) = useState<((Number) -> String)?>(null)
    useEffect {
        val value = interpolatorAsync.await()
        setInterpolator(transform = { value })
    }
    interpolator ?: return@nfc

    val inclusiveContributions = adjustDatasetForHeatMap(
        contributionData.toMap().mapValues { (_, report) -> report.contributions?.elements ?: emptyList() },
    )
    if (inclusiveContributions.values.flatten().isEmpty()) {
        +"No contributions available for this period."
        return@nfc
    }

    val (max, data: Array<NivoHeatMapData>) = inclusiveContributions.toNivoHeatmapSettings(
        window,
        spinsUntilFullRotation,
        yConverter = { it.size },
    )
    val allSolos = contributionData.toMap().keys.flatten().map { pairOf(it) }.toSet()
    colorContext.Provider {
        this.value = getColor
        pairContext {
            this.value = allSolos
            ResponsiveHeatMap {
                legend = "Pair Commits"
                this.data = data
                colors = { datum -> interpolator(datum.value.toDouble() / max) }
                emptyColor = interpolator(0)
                margin = NivoChartMargin(
                    top = 65,
                    right = 90,
                    bottom = 60,
                    left = 90,
                )
                tooltip = CouplingHeatmapTooltip
                axisLeft = NivoAxis(
                    tickSize = 5,
                    tickPadding = 5,
                    legendOffset = -52,
                    tickRotation = 90,
                    truncateTickAt = 0,
                    ticksPosition = "before",
                    renderTick = PairTickMark,
                )
                axisTop = NivoAxis(
                    tickSize = 5,
                    tickPadding = 5,
                    tickRotation = 180,
                    legendOffset = -30,
                    truncateTickAt = 0,
                    renderTick = PairTickMark,
                )
                axisRight = NivoAxis(
                    tickSize = 5,
                    tickPadding = 5,
                    tickRotation = -90,
                    legend = "",
                    legendPosition = "middle",
                    legendOffset = 70,
                    truncateTickAt = 0,
                    renderTick = PairTickMark,
                )
                axisBottom = NivoAxis(
                    renderTick = PairTickMark,
                    tickRotation = 0,
                )
            }
        }
    }
}
