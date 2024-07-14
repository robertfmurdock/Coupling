package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.stats.NinoLinePoint
import com.zegreatrob.coupling.client.components.stats.NivoLineData
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
    var window: JsonContributionWindow?
}

@ReactFunc
val PairFrequencyLineGraph by nfc<PairFrequencyLineGraphProps> { (data, window) ->
    val duration = window?.toModel()

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
            NinoLinePoint(
                x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
                y = it.value.size,
                context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
            )
        }.toTypedArray(),
    )
