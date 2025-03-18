package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoLineData
import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoPoint
import com.zegreatrob.coupling.client.components.graphing.external.recharts.RechartsTooltipArgs
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
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
import react.FC
import react.Props
import react.PropsWithValue
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.px

external interface EaseGraphProps : Props {
    var data: List<Pair<CouplingPair, ContributionReport>>
    var window: GqlContributionWindow
}

@ReactFunc
val EaseGraph by nfc<EaseGraphProps> { (data, window) ->
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

val LineTooltip = FC<PropsWithValue<RechartsTooltipArgs>> { props ->
    val args = props.value
    div {
        css {
            backgroundColor = Color("rgb(0 0 0 / 14%)")
            padding = 10.px
            borderRadius = 20.px
        }
        args.payload?.forEach { payload ->
            div {
                key = payload.name.toString()
                +"${payload.name} - ${payload.value}"
            }
        }
        div { +args.labelFormatter(args.label) }
    }
}

private fun easeLineData(selectedPairs: List<Pair<CouplingPair, ContributionReport>>): Array<NivoLineData> = selectedPairs.map { pairContributionLine(it.first, it.second.contributions?.elements ?: emptyList()) }
    .toTypedArray()

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>) = NivoLineData(
    couplingPair.joinToString("-") { it.name },
    contributions.groupBy { contribution ->
        contribution.dateTime
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
    }.mapNotNull {
        val date = it.key ?: return@mapNotNull null
        if (it.value.isEmpty()) return@mapNotNull null

        NivoPoint(
            x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
            y = it.value.mapNotNull { it.ease }.average(),
            context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
        )
    }.toTypedArray(),
)
