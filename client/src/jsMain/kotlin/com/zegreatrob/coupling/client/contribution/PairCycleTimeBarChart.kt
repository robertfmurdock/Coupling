package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.external.nivo.AxisTickProps
import com.zegreatrob.coupling.client.components.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.bar.ResponsiveBar
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.FC
import react.Props
import react.dom.svg.ReactSVG.foreignObject
import react.dom.svg.ReactSVG.g
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

external interface PairCycleTimeBarChartProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

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
    val formatMillisAsDuration: (Number) -> String = { value ->
        (Duration.ZERO + value.toLong().milliseconds).toString()
    }

    ResponsiveBar {
        this.data = data
        this.keys = arrayOf("value")
        this.indexBy = "pair"
        margin = NivoChartMargin(
            top = 65,
            right = 90,
            bottom = 60,
            left = 90,
        )
        this.valueFormat = formatMillisAsDuration
        this.axisLeft = NivoAxis(
            format = formatMillisAsDuration,
        )
        this.axisBottom = NivoAxis(
            renderTick = PairTickMark,
        )
        this.layout = "vertical"
        this.labelPosition = "end"
        this.labelOffset = -10
        this.groupMode = "grouped"
    }
}

val PairTickMark = FC<AxisTickProps> { props ->
    val pair = props.value.unsafeCast<CouplingPair>()
    val elementWidth = pair.count() * 35.0
    g {
        transform = "translate(${props.x.toDouble() - elementWidth / 2.0}, ${props.y.toDouble() + 10})"
        foreignObject {
            width = elementWidth
            height = 45.0
            TiltedPlayerList(playerList = pair, size = 25)
        }
    }
}
