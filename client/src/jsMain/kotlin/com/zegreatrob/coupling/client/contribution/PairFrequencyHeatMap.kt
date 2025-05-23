package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.PairTickMark
import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.colorContext
import com.zegreatrob.coupling.client.components.create
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.ResponsiveHeatMap
import com.zegreatrob.coupling.client.components.external.nivo.heatmap.TooltipProps
import com.zegreatrob.coupling.client.components.external.nivo.tooltip.BasicTooltip
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoHeatMapData
import com.zegreatrob.coupling.client.components.graphing.interpolatorAsync
import com.zegreatrob.coupling.client.components.pairContext
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.stats.adjustDatasetForHeatMap
import com.zegreatrob.coupling.client.components.stats.toNivoHeatmapSettings
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.FC
import react.Props
import react.use
import react.useEffect
import react.useState
import web.cssom.Angle
import kotlin.collections.component1
import kotlin.collections.component2

external interface PairContributionsHeatMapProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
    var spinsUntilFullRotation: Int
}

@ReactFunc
val PairContributionsHeatMap by nfc<PairContributionsHeatMapProps> { (contributionData, window, spinsUntilFullRotation) ->
    val getColor = useOrdinalColorScale(unsafeJso { scheme = "pastel1" }, "value")
    val (interpolator, setInterpolator) = useState<((Number) -> String)?>(null)
    useEffect {
        val value = interpolatorAsync.await()
        setInterpolator(transform = { value })
    }
    interpolator ?: return@nfc

    val inclusiveContributions = adjustDatasetForHeatMap(
        contributionData.toMap().mapValues { (_, report) -> report.contributions?.elements ?: emptyList() },
    )
    val (max, data: Array<NivoHeatMapData>) = inclusiveContributions.toNivoHeatmapSettings(
        window,
        spinsUntilFullRotation,
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

val CouplingHeatmapTooltip = FC<TooltipProps> { props ->
    val cell = props.cell
    val pairs = use(pairContext)
    val flatten = pairs.flatten()
    val players = flatten.filter { cell.id.split(".").contains(it.id.value.toString()) }
    val pair = players.toCouplingPair()

    if (cell.formattedValue === null) return@FC

    BasicTooltip {
        id = TiltedPlayerList.create(playerList = pair, children = { tilt: Angle, player: Player ->
            PlayerCard(player, tilt = tilt, size = 35, key = player.id.value.toString())
        })
        value = cell.formattedValue
        enableChip = true
        color = cell.color
    }
}
