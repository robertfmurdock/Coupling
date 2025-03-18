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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.create

external interface PairFrequencyLineGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val PairFrequencyLineGraph by nfc<PairFrequencyLineGraphProps> { (data, window) ->
    val duration = window.toModel()

    if (data.flatMap { it.second.contributions?.elements ?: emptyList() }.isNotEmpty()) {
        CouplingResponsiveLine {
            legend = "Pair Commits Over Time"
            this.data = pairingLineData(data)

            if (duration != null) {
                this.xMin = (Clock.System.now() - duration).toJSDate()
            }
            this.xMax = Clock.System.now().toJSDate()
            tooltip = { args -> LineTooltip.create { value = args } }
        }
    }
}

private fun pairingLineData(selectedPairs: List<Pair<CouplingPair, ContributionReport>>): Array<NivoLineData> = selectedPairs.map { pairContributionLine(it.first, it.second.contributions?.elements ?: emptyList()) }
    .toTypedArray()

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>) = NivoLineData(
    couplingPair.joinToString("-") { it.name },
    contributions.groupBy { contribution ->
        contribution.dateTime
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
    }.mapNotNull {
        val date = it.key ?: return@mapNotNull null
        NivoPoint(
            x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
            y = it.value.size,
            context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
        )
    }.toTypedArray(),
)
