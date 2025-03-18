package com.zegreatrob.coupling.client.contribution

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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import react.Props
import react.create

external interface PairEaseLineGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairEaseLineGraph by nfc<PairEaseLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.flatMap { it.second.contributions?.elements ?: emptyList() }.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Daily Ease Over Time"
            this.data = easeLineData(data)
            yAxisDomain = arrayOf(0, 5)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    }
}

private fun easeLineData(selectedPairs: List<Pair<CouplingPair, ContributionReport>>): Array<NivoLineData> = selectedPairs.map { pairContributionLine(it.first, it.second.contributions?.elements ?: emptyList()) }
    .toTypedArray()

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>) = NivoLineData(
    couplingPair.joinToString("-") { it.name },
    contributions.groupBy(contributionsByDate)
        .mapNotNull(::dateContributionGroupToAverageEasePoint)
        .toTypedArray(),
)

fun dateContributionGroupToAverageEasePoint(it: Map.Entry<LocalDate?, List<Contribution>>): NivoPoint? {
    val date = it.key ?: return null
    if (it.value.isEmpty()) return null

    return NivoPoint(
        x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
        y = it.value.mapNotNull { it.ease }.average(),
        context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
    )
}
