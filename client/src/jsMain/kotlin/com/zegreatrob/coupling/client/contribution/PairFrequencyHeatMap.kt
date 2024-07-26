package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.ResponsiveHeatMap
import com.zegreatrob.coupling.client.components.stats.adjustDatasetForHeatMap
import com.zegreatrob.coupling.client.components.stats.heatmap.interpolatorAsync
import com.zegreatrob.coupling.client.components.stats.toNivoHeatmapSettings
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.useEffect
import react.useState

external interface PairFrequencyHeatMapProps : Props {
    var data: List<Pair<CouplingPair, List<Contribution>>>
    var window: GqlContributionWindow
    var spinsUntilFullRotation: Int
}

@ReactFunc
val PairFrequencyHeatMap by nfc<PairFrequencyHeatMapProps> { (contributionData, window, spinsUntilFullRotation) ->
    val (interpolator, setInterpolator) = useState<((Number) -> String)?>(null)
    useEffect {
        val value = interpolatorAsync.await()
        setInterpolator(transform = { value })
    }
    interpolator ?: return@nfc

    val inclusiveContributions = adjustDatasetForHeatMap(contributionData.toMap())
    val (max, data: Array<NivoHeatMapData>) = inclusiveContributions.toNivoHeatmapSettings(
        window,
        spinsUntilFullRotation,
    )

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
        axisLeft = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = 0,
            legend = "Player",
            legendPosition = "middle",
            legendOffset = -52,
            truncateTickAt = 0,
        )
        axisTop = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = -90,
            legend = "Player",
            legendPosition = "middle",
            legendOffset = -30,
            truncateTickAt = 0,
        )
        axisRight = NivoHeatMapAxis(
            tickSize = 5,
            tickPadding = 5,
            tickRotation = 0,
            legend = "",
            legendPosition = "middle",
            legendOffset = 70,
            truncateTickAt = 0,
        )
    }
}
