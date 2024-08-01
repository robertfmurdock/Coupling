package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.NivoOrdinalScaleColorConfig
import com.zegreatrob.coupling.client.components.external.nivo.bar.ResponsiveBar
import com.zegreatrob.coupling.client.components.external.nivo.colors.useOrdinalColorScale
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Props
import react.createContext
import kotlin.js.json

external interface PairCycleTimeBarChartProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

val colorContext = createContext<dynamic>()

@ReactFunc
val PairCycleTimeBarChart by nfc<PairCycleTimeBarChartProps> { props ->
    val pairToCycleTime =
        props.data.mapNotNull { (pair, report) -> pair to (report.medianCycleTime ?: return@mapNotNull null) }

    val data = pairToCycleTime.map { (pair, cycleTime) ->
        jso<dynamic> {
            this.pair = pair
            value = cycleTime.inWholeMilliseconds
        }
    }.toTypedArray()
    val largestMobSize = pairToCycleTime.toMap().keys.largestMobSize()
    val getColor = useOrdinalColorScale(jso { scheme = "pastel1" }, "value")
    colorContext.Provider {
        this.value = getColor
        ResponsiveBar {
            this.data = data
            this.keys = arrayOf("value")
            this.indexBy = "pair"
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
                "${data.indexValue.unsafeCast<CouplingPair>().pairName} - ${data.formattedValue}"
            }
        }
    }
}

fun Set<CouplingPair>.largestMobSize() = maxOfOrNull { it.count() } ?: 2
