package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import com.zegreatrob.coupling.client.components.external.nivo.bar.ResponsiveBar
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.dom.html.ReactHTML.div
import kotlin.js.json

external interface PairCycleTimeBarChartProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: ContributionWindow
}

@ReactFunc
val PairCycleTimeBarChart by nfc<PairCycleTimeBarChartProps> { props ->
    val pairToCycleTime =
        props.data.mapNotNull { (pair, report) -> pair to (report.medianCycleTime ?: return@mapNotNull null) }

    val data = pairToCycleTime.map { (pair, cycleTime) ->
        unsafeJso<dynamic> {
            this.pairId = pair.pairId
            value = cycleTime.inWholeMilliseconds.toInt()
        }
    }.toTypedArray()
    div {
        asDynamic()["data-testid"] = "pair-cycle-time-bar-chart"
        val largestMobSize = pairToCycleTime.toMap().keys.largestMobSize()
        val getColor = useOrdinalColorScale(NivoOrdinalScaleColorConfig(scheme = "pastel1"), "value")
        val couplingPairs = props.data.toMap().keys
        colorContext.Provider {
            this.value = getColor
            pairContext {
                this.value = couplingPairs
                ResponsiveBar {
                    this.data = data
                    this.keys = arrayOf("value")
                    this.indexBy = "pairId"
                    margin = NivoChartMargin(
                        top = 65,
                        right = 90,
                        bottom = 10 + ESTIMATED_PLAYER_WIDTH * largestMobSize,
                        left = 90,
                    )
                    this.valueFormat = formatMillisAsDuration
                    colors = { arg: dynamic -> getColor(json("value" to arg.indexValue)) }
                        .unsafeCast<NivoOrdinalScaleColorConfig>()
                    colorBy = "indexValue"
                    this.axisLeft = NivoAxis(
                        format = formatMillisAsDuration,
                    )
                    this.axisBottom = NivoAxis(renderTick = PairTickMark)
                    this.layout = "vertical"
                    this.labelPosition = "end"
                    this.labelOffset = -10
                    this.groupMode = "grouped"
                    tooltipLabel = { data ->
                        "${couplingPairs.find { it.pairId == data.indexValue }?.pairName} - ${data.formattedValue}"
                    }
                }
            }
        }
    }
}

fun Set<CouplingPair>.largestMobSize() = maxOfOrNull { it.count() } ?: 2
