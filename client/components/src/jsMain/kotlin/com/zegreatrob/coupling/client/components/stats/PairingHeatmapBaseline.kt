package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.stats.heatmap.PairingBaseline
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
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.dd
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.dl
import react.dom.html.ReactHTML.dt
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.strong
import react.dom.html.ReactHTML.summary
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import web.aria.AriaRole
import web.aria.group
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.Border
import web.cssom.BorderCollapse
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Cursor
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.LineStyle
import web.cssom.Normal
import web.cssom.TextAlign
import web.cssom.pct
import web.cssom.px

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
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 8.px
            width = (62 + props.players.size * 90).px
            maxWidth = 100.pct
            minWidth = 220.px
            marginTop = 10.px
            whiteSpace = Normal.normal
            textAlign = TextAlign.left
            color = Color("#34214f")
        }
        heatmapKey()
        div {
            role = AriaRole.group
            ariaLabel = "Pairing distribution"
            css { heatmapInformationPanel(Color("#75429c")) }
            div {
                css {
                    padding = 8.px
                    fontSize = 14.px
                    backgroundColor = Color("#75429c")
                    color = Color("#fff")
                    borderTopLeftRadius = 8.px
                    borderTopRightRadius = 8.px
                    borderBottom = Border(2.px, LineStyle.solid, Color("#75429c"))
                    fontWeight = FontWeight.bold
                }
                +"Pairing distribution"
            }
            referenceSpotlight(baseline)
            referenceExplanation()
            pairBreakdown(props.players, props.pairs, baseline)
        }
    }
}

private fun ChildrenBuilder.referenceSpotlight(baseline: PairingBaseline?) = dl {
    css {
        display = Display.flex
        flexWrap = FlexWrap.wrap
        alignItems = AlignItems.center
        justifyContent = JustifyContent.spaceBetween
        gap = 10.px
        margin = 0.px
        padding = 8.px
    }
    dt {
        strong { +"Even split per pair" }
        if (baseline != null) {
            div {
                css {
                    fontSize = 12.px
                    marginTop = 5.px
                }
                span { +"${baseline.savedSpinsInWindow} saved spins" }
                +" · "
                span { +"${baseline.spinsUntilFullRotation} spins / rotation" }
            }
        }
    }
    dd {
        css {
            margin = 0.px
            padding = 4.px
            minWidth = 56.px
            textAlign = TextAlign.center
            fontSize = if (baseline == null) 16.px else 28.px
            fontWeight = FontWeight.bold
            color = Color("#432800")
            backgroundColor = Color("#ffcf4a")
            border = Border(2.px, LineStyle.solid, Color("#855000"))
            borderRadius = 8.px
            boxShadow = BoxShadow(2.px, 2.px, Color("#855000"))
        }
        +(baseline?.expectedPairings?.toFixed(1) ?: "Unavailable")
    }
}

private fun ChildrenBuilder.referenceExplanation() = details {
    css {
        padding = 8.px
        paddingTop = 0.px
        fontSize = 12.px
    }
    summary {
        css {
            cursor = Cursor.pointer
            padding = 3.px
        }
        +"About this comparison"
    }
    div {
        css { padding = 6.px }
        +"What the counts would be if pairings were spread evenly across the current team."
    }
    div {
        css {
            padding = 6.px
            paddingTop = 0.px
        }
        +"Uses up to five full rotations of saved history. Absences, pins and team changes can all shape the counts."
    }
}

private fun ChildrenBuilder.pairBreakdown(players: List<Player>, pairs: List<PlayerPair>, baseline: PairingBaseline?) = details {
    summary {
        css {
            padding = 8.px
            fontSize = 14.px
            cursor = Cursor.pointer
            fontWeight = FontWeight.bold
            color = Color("#184f48")
            backgroundColor = Color("#a9e7ce")
            borderTop = Border(2.px, LineStyle.solid, Color("#438177"))
            borderBottomLeftRadius = 8.px
            borderBottomRightRadius = 8.px
            ":hover" { backgroundColor = Color("#91dcbc") }
        }
        span { +"Pair breakdown" }
        span {
            css {
                fontSize = 12.px
                fontWeight = Normal.normal
                marginLeft = 8.px
            }
            +"${players.size * (players.size - 1) / 2} pairs"
        }
    }
    div {
        role = AriaRole.group
        ariaLabel = "Scrollable pair details"
        tabIndex = 0
        css {
            overflowX = Auto.auto
            padding = 6.px
            minWidth = 0.px
        }
        table {
            css {
                width = 100.pct
                borderCollapse = BorderCollapse.collapse
                fontSize = 12.px
                "th, td" {
                    padding = 7.px
                    textAlign = TextAlign.left
                    borderBottom = Border(1.px, LineStyle.solid, Color("#d9cce5"))
                }
                "thead" {
                    backgroundColor = Color("#75429c")
                    color = Color("#fff")
                }
                "tbody tr:nth-of-type(even)" { backgroundColor = Color("#f4edff") }
            }
            caption {
                css {
                    padding = 6.px
                    fontWeight = FontWeight.bold
                    textAlign = TextAlign.left
                }
                +"Recent pairings · even split ${baseline?.expectedPairings?.toFixed(1) ?: "unavailable"} per pair"
            }
            thead {
                tr {
                    listOf("Pair", "Count", "vs even split", "Spins ago").forEach { label ->
                        th {
                            scope = "col"
                            +label
                        }
                    }
                }
            }
            tbody {
                players.forEachIndexed { index, first ->
                    players.drop(index + 1).forEach { second ->
                        val pair = pairs.pairFor(first, second)
                        val actual = pair?.recentTimesPaired
                        tr {
                            th {
                                scope = "row"
                                +"${first.name} / ${second.name}"
                            }
                            td { +(actual?.toString() ?: "Unavailable") }
                            td {
                                ariaLabel = pairingDifference(actual, baseline?.expectedPairings)
                                span {
                                    css {
                                        display = Display.inlineBlock
                                        padding = 4.px
                                        borderRadius = 5.px
                                        fontWeight = FontWeight.bold
                                        backgroundColor = Color("#eee5fa")
                                    }
                                    +compactPairingDifference(actual, baseline?.expectedPairings)
                                }
                            }
                            td { +pair.lastPairedLabel() }
                        }
                    }
                }
            }
        }
    }
}

private fun compactPairingDifference(actual: Int?, comparison: Double?): String {
    if (actual == null || comparison == null) return "Unavailable"
    val difference = actual - comparison
    return (if (difference > 0) "+" else "") + difference.toFixed(1)
}

fun pairingHeatmapCells(players: List<Player>, pairs: List<PlayerPair>, spinsUntilFullRotation: Int, savedSpinCount: Int?): List<List<HeatmapCell>> {
    val comparison = pairingBaseline(savedSpinCount, spinsUntilFullRotation)?.expectedPairings
    return heatmapData(players, pairs).mapIndexed { row, values ->
        values.mapIndexed { column, value ->
            if (row == column) HeatmapCell(value) else pairingHeatmapCell(value, players[row], players[column], pairs, comparison)
        }
    }
}

private fun pairingHeatmapCell(value: Double?, first: Player, second: Player, pairs: List<PlayerPair>, comparison: Double?): HeatmapCell {
    val pair = pairs.pairFor(first, second)
    val actual = pair?.recentTimesPaired
    val description = "${first.name} / ${second.name}. Recent pairings: ${actual ?: "Unavailable"}. " +
        "Even split: ${comparison?.toFixed(1) ?: "Unavailable"} per pair. ${pairingDifference(actual, comparison)}. " +
        "Spins since last paired: ${pair.lastPairedLabel()}."
    return HeatmapCell(value, description)
}

private fun List<PlayerPair>.pairFor(first: Player, second: Player) = firstOrNull {
    it.players.elements.map(Player::id).toSet() == setOf(first.id, second.id)
}

private fun PlayerPair?.lastPairedLabel() = if (this == null) "Unavailable" else spinsSinceLastPaired?.toString() ?: "Never paired"

private fun pairingDifference(actual: Int?, comparison: Double?): String {
    if (actual == null || comparison == null) return "Unavailable"
    val difference = actual - comparison
    return when {
        difference > 0 -> "${difference.toFixed(1)} more than an even split"
        difference < 0 -> "${(-difference).toFixed(1)} fewer than an even split"
        else -> "Same count as an even split"
    }
}
