package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.action.stats.heatmap.heatmapData
import com.zegreatrob.coupling.action.timeSincePairSort
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.model.PlayerPair
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
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.Display
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.number
import kotlin.time.Duration

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance: (Int?, Int) -> String = if (formatDistanceModule.default != undefined) {
    formatDistanceModule.default.unsafeCast<(Int?, Int) -> String>()
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
}

@ReactFunc
val PartyStatistics by nfc<PartyStatisticsProps> { props ->
    val (party, players, pairs, spinsUntilFullRotation, medianSpinDuration) = props
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = party
                +"Statistics"
            }
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
                            medianSpinDuration = medianSpinDuration?.let {
                                formatDistance(medianSpinDuration.inWholeMilliseconds.toInt(), 0)
                            } ?: "",
                        )
                    }
                    PairReportTable(
                        pairs.map {
                            it.players?.elements?.toCouplingPair() to it.spinsSinceLastPaired
                        }.mapNotNull {
                            val pair = it.first as? (CouplingPair.Double)
                                ?: return@mapNotNull null
                            val spins = it.second?.let(::TimeResultValue) ?: NeverPaired
                            pair to spins
                        }.map { PairReport(it.first, it.second) }
                            .sortedByDescending(::timeSincePairSort),
                    )
                }
                PlayerHeatmap(players, heatmapData(players, pairs))
            }
        }
    }
}
