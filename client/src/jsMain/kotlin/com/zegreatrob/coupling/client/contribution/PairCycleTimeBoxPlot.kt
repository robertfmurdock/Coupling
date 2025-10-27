package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.ESTIMATED_PLAYER_WIDTH
import com.zegreatrob.coupling.client.components.PairTickMark
import com.zegreatrob.coupling.client.components.colorContext
import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import com.zegreatrob.coupling.client.components.external.nivo.boxplot.ResponsiveBoxPlot
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.client.components.formatMillisAsDuration
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.largestMobSize
import com.zegreatrob.coupling.client.components.pairContext
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import kotlin.js.json

external interface PairCycleTimeBoxPlotProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairCycleTimeBoxPlot by nfc<PairCycleTimeBoxPlotProps> { props ->
    val getColor = useOrdinalColorScale(NivoOrdinalScaleColorConfig(scheme = "pastel1"), "value")
    val couplingPairs = props.data.toMap().keys
    colorContext.Provider {
        this.value = getColor
        pairContext {
            this.value = couplingPairs
            ResponsiveBoxPlot {
                data = props.data.toCycleTimeData()
                groupBy = "pairId"
                valueFormat = formatMillisAsDuration
                colors = { arg: dynamic -> getColor(json("value" to arg.group)) }
                    .unsafeCast<NivoOrdinalScaleColorConfig>()
                colorBy = "group"
                margin = NivoChartMargin(
                    top = 65,
                    right = 90,
                    bottom = 10 + ESTIMATED_PLAYER_WIDTH * couplingPairs.largestMobSize(),
                    left = 90,
                )
                this.axisLeft = NivoAxis(
                    format = formatMillisAsDuration,
                )
                this.axisBottom = NivoAxis(renderTick = PairTickMark)
                tooltipLabel = { data -> couplingPairs.find { it.pairId == data.group }?.pairName ?: "?" }
            }
        }
    }
}

private fun List<Pair<CouplingPair, ContributionReport>>.toCycleTimeData() = flatMap { (pair, report) ->
    report.contributions?.elements?.mapNotNull { contribution ->
        unsafeJso<dynamic> {
            pairId = pair.pairId
            this.pair = pair
            value = contribution.cycleTime?.inWholeMilliseconds ?: return@mapNotNull null
        }
    } ?: emptyList()
}.toTypedArray()
