package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.action.stats.heatmap.heatmapData
import com.zegreatrob.coupling.action.timeSincePairSort
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.toJSDate
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.number
import web.cssom.px
import kotlin.time.Duration

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance: (Int?, Int) -> String = if (formatDistanceModule["formatDistance"] != undefined) {
    formatDistanceModule["formatDistance"].unsafeCast<(Int?, Int) -> String>()
} else {
    formatDistanceModule.unsafeCast<(Int?, Int) -> String>()
}

external interface PartyStatisticsProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: List<PlayerPair>
    var spinsUntilFullRotation: Int

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var medianSpinDuration: Duration?
    var chartComponent: FC<CouplingResponsiveLineProps>?
}

@ReactFunc
val PartyStatistics by nfc<PartyStatisticsProps> { props ->
    val (party, players, pairs, spinsUntilFullRotation, medianSpinDuration, chartComponent) = props
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = party
                +"Statistics"
            }
            PartyStatisticsContent(spinsUntilFullRotation, players, medianSpinDuration, pairs, chartComponent)
        }
    }
}

external interface PartyStatisticsContentProps : Props {
    var spinsUntilFullRotation: Int
    var players: List<Player>

    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var medianSpinDuration: Duration?
    var pairs: List<PlayerPair>
    var chartComponent: FC<CouplingResponsiveLineProps>?
}

@ReactFunc
val PartyStatisticsContent by nfc<PartyStatisticsContentProps> { props ->
    val (spinsUntilFullRotation, players, medianSpinDuration, pairs) = props

    var showPlot by useState(false)

    div {
        css {
            whiteSpace = WhiteSpace.nowrap
            display = Display.inlineFlex
        }
        div {
            css {
                display = Display.inlineBlock
                verticalAlign = VerticalAlign.top
                flexGrow = number(0.0)
            }
            div {
                TeamStatistics(
                    spinsUntilFullRotation = spinsUntilFullRotation,
                    activePlayerCount = players.size,
                    medianSpinDuration = medianSpinDuration,
                )
            }
            PairReportTable(pairs.pairReports())
        }
        div {
            css {
                display = Display.inlineBlock
                marginLeft = 20.px
            }
            div {
                CouplingButton(onClick = { showPlot = false }) { +"Heatmap" }
                CouplingButton(onClick = { showPlot = true }) { +"Recent Pairing" }
            }

            if (showPlot) {
                div {
                    css {
                        width = 600.px
                        height = 600.px
                        backgroundColor = Color("white")
                    }
                    props.chartComponent?.invoke {
                        data = props.pairs.nivoPairHeatLineData()
                        legend = "Recent Pair Count"
                    }
                }
            } else {
                PlayerHeatmap(players, heatmapData(players, pairs))
            }
        }
    }
}

private fun List<PlayerPair>.nivoPairHeatLineData() = filter { it.players?.size == 2 }
    .map {
        NivoLineData(
            id = it.players?.joinToString("-") { it.element.name } ?: "unknown",
            data = it.pairAssignmentHistory
                ?.map { pairAssignment ->
                    NinoPoint(
                        x = pairAssignment.date?.toJSDate() ?: 0,
                        y = pairAssignment.recentTimesPaired ?: 0,
                    )
                }
                ?.toTypedArray() ?: emptyArray(),
        )
    }.filter { it.data.isNotEmpty() }
    .toTypedArray()

sealed external interface NivoLineData {
    var id: String
    var data: Array<NinoPoint>
    var color: String?
}

sealed external interface NinoPoint {
    var x: Any?
    var y: Any?
    var context: Any?
}

sealed external interface NinoLinePointDecorated {
    var x: Any
    var xFormatted: Any
    var y: Any
    var yFormatted: Any
    var context: Any?
}

private fun List<PlayerPair>.pairReports() = map { it.players?.elements?.toCouplingPair() to it.spinsSinceLastPaired }
    .mapNotNull { (pair, spins) ->
        (pair as? CouplingPair.Double)?.let {
            PairReport(
                pair = it,
                timeSinceLastPair = spins?.let(::TimeResultValue) ?: NeverPaired,
            )
        }
    }
    .sortedByDescending(::timeSincePairSort)

external interface CouplingResponsiveLineProps : Props {
    var data: Array<NivoLineData>
    var legend: String
    var tooltip: (NinoLinePointDecorated) -> ReactNode
    var xMin: kotlin.js.Date
    var xMax: kotlin.js.Date
}

external interface CouplingResponsiveHeatMapProps : Props {
    var data: Array<NivoHeatMapData>
    var legend: String
    var colors: NivoHeatMapColors
}

sealed external interface NivoHeatMapData {
    var id: String
    var data: Array<NinoPoint>
}

sealed external interface NivoHeatMapColors {
    var type: String
    var scheme: String
    var divergeAt: Number
    var minValue: Number
    var maxValue: Number
}
