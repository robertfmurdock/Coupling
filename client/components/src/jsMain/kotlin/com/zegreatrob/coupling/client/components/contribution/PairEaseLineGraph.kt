package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoPoint
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import react.Props
import react.create
import kotlin.time.Clock
import kotlin.time.toJSDate

external interface PairEaseLineGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairEaseLineGraph by nfc<PairEaseLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.isEmpty()) {
        +"No pairs are selected."
        return@nfc
    }
    val pairContributionsGroupedByDate = data
        .map {
            it.first to (it.second.contributions?.elements ?: emptyList()).groupBy(contributionsByDate)
                .mapNotNull(::dateContributionGroupToAverageEasePoint)
        }

    if (pairContributionsGroupedByDate.flatMap { it.second }.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Daily Ease Over Time"
            this.data = pairContributionsGroupedByDate
                .map { (first, second) -> NivoLineData(first.joinToString("-") { it.name }, second.toTypedArray()) }
                .toTypedArray()
            yAxisDomain = arrayOf(0, 5)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    } else {
        +"No ease data available for this period."
    }
}

fun dateContributionGroupToAverageEasePoint(it: Map.Entry<LocalDate?, List<Contribution>>): NivoPoint? {
    val date = it.key ?: return null
    val easeValues = it.value.mapNotNull { it.ease }
    if (easeValues.isEmpty()) return null

    return NivoPoint(
        x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
        y = easeValues.average(),
        context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
    )
}
