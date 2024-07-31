package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.external.nivo.NivoAxis
import com.zegreatrob.coupling.client.components.external.nivo.NivoChartMargin
import com.zegreatrob.coupling.client.components.external.nivo.bar.ResponsiveBar
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Props
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
            this.pair = pair.pairName
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
        this.layout = "vertical"
        this.labelPosition = "end"
        this.labelOffset = -10
        this.groupMode = "grouped"
    }
}
