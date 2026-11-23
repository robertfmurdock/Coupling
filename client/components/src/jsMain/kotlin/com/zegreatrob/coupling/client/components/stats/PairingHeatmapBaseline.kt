package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.heatmap.heatmapData
import com.zegreatrob.coupling.action.stats.heatmap.pairingBaseline
import com.zegreatrob.coupling.client.components.graphing.HeatmapCell
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.numbers.toFixed
import react.Props
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.summary
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import web.cssom.Normal
import web.cssom.px
import kotlin.math.abs

external interface PairingHeatmapBaselineProps : Props {
    var players: List<Player>
    var pairs: List<PlayerPair>
    var spinsUntilFullRotation: Int
    var savedSpinCount: Int?
}

@ReactFunc
val PairingHeatmapBaseline by nfc<PairingHeatmapBaselineProps> { props ->
    val baseline = pairingBaseline(props.savedSpinCount, props.spinsUntilFullRotation)
    div {
        css {
            whiteSpace = Normal.normal
            maxWidth = (62 + props.players.size * 90).px
        }
        div { +"Color scale: blue = 0, red = 5 or more; gray = no count." }
        div { +"Outlined cells are within one pairing of expected." }
        div { +"Expected pairings per pair: ${baseline?.expectedPairings?.toFixed(1) ?: "Unavailable"}" }
        if (baseline != null) {
            div { +"Based on ${baseline.savedSpinsInWindow} saved spins; one full rotation takes ${baseline.spinsUntilFullRotation} spins." }
        }
        div { +"Equal-rotation reference for the current team; absences, pins and team changes can affect counts." }
        details {
            summary { +"Pairing counts and baseline" }
            table {
                caption { +"Recent pairings in the heatmap window (up to five full rotations)" }
                thead {
                    tr {
                        listOf("Pair", "Recent pairings", "Expected", "Difference", "Spins since last paired").forEach { label ->
                            th {
                                scope = "col"
                                +label
                            }
                        }
                    }
                }
                tbody {
                    props.players.forEachIndexed { index, first ->
                        props.players.drop(index + 1).forEach { second ->
                            val pair = props.pairs.pairFor(first, second)
                            val actual = pair?.recentTimesPaired
                            tr {
                                th {
                                    scope = "row"
                                    +"${first.name} / ${second.name}"
                                }
                                td { +(actual?.toString() ?: "Unavailable") }
                                td { +(baseline?.expectedPairings?.toFixed(1) ?: "Unavailable") }
                                td { +pairingDifference(actual, baseline?.expectedPairings) }
                                td { +pair.lastPairedLabel() }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun pairingHeatmapCells(players: List<Player>, pairs: List<PlayerPair>, spinsUntilFullRotation: Int, savedSpinCount: Int?): List<List<HeatmapCell>> {
    val expected = pairingBaseline(savedSpinCount, spinsUntilFullRotation)?.expectedPairings
    return heatmapData(players, pairs).mapIndexed { row, values ->
        values.mapIndexed { column, value ->
            if (row == column) HeatmapCell(value) else pairingHeatmapCell(value, players[row], players[column], pairs, expected)
        }
    }
}

private fun pairingHeatmapCell(value: Double?, first: Player, second: Player, pairs: List<PlayerPair>, expected: Double?): HeatmapCell {
    val pair = pairs.pairFor(first, second)
    val actual = pair?.recentTimesPaired
    val description = "${first.name} / ${second.name}. Recent pairings: ${actual ?: "Unavailable"}. " +
        "Expected: ${expected?.toFixed(1) ?: "Unavailable"}. ${pairingDifference(actual, expected)}. " +
        "Spins since last paired: ${pair.lastPairedLabel()}."
    return HeatmapCell(value, description, actual != null && expected != null && abs(actual - expected) <= 1)
}

private fun List<PlayerPair>.pairFor(first: Player, second: Player) = firstOrNull {
    it.players.elements.map(Player::id).toSet() == setOf(first.id, second.id)
}

private fun PlayerPair?.lastPairedLabel() = if (this == null) "Unavailable" else spinsSinceLastPaired?.toString() ?: "Never paired"

private fun pairingDifference(actual: Int?, expected: Double?): String {
    if (actual == null || expected == null) return "Unavailable"
    val difference = actual - expected
    return when {
        difference > 0 -> "${difference.toFixed(1)} above expected"
        difference < 0 -> "${(-difference).toFixed(1)} below expected"
        else -> "At expected frequency"
    }
}
