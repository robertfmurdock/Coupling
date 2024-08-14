package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.external.d3.array.D3Array
import com.zegreatrob.coupling.client.components.stats.ContributionControlPanelFrame
import com.zegreatrob.coupling.client.components.stats.ContributionLabelFilter
import com.zegreatrob.coupling.client.components.stats.EnumSelector
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h4
import react.useEffect
import react.useState
import web.cssom.Display
import web.cssom.em
import web.cssom.fr
import web.cssom.ident
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

external interface ContributionListContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
    var contributors: List<Player>
    var window: GqlContributionWindow
    var setWindow: (GqlContributionWindow) -> Unit
}

@ReactFunc
val ContributionListContent by nfc<ContributionListContentProps> { (_, contributions, contributors, window, setWindow) ->
    val allLabels = contributions.mapNotNull(Contribution::label).toSet()
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    val filteredContributions = selectedLabelFilter?.let { contributions.filter { it.label == selectedLabelFilter } }
        ?: contributions
    val (d3Array, setD3Array) = useState<D3Array?>(null)
    useEffect { setD3Array(com.zegreatrob.coupling.client.components.external.d3.array.d3Array.await()) }

    div {
        div {
            css { display = Display.inlineBlock }
            h2 {
                +"Contributions for the last $window:"
            }
            div {
                ContributionControlPanelFrame {
                    div {
                        EnumSelector(
                            default = window,
                            onChange = setWindow,
                            label = ReactNode("Time Window"),
                            backgroundColor = contributionContentBackgroundColor,
                        )
                    }
                    div {
                        ContributionLabelFilter(
                            allLabels = allLabels,
                            selected = selectedLabelFilter,
                            setSelected = setSelectedLabelFilter::invoke,
                        )
                    }
                    val cycleTimes = filteredContributions
                        .mapNotNull { it.cycleTime }
                    if (cycleTimes.isNotEmpty()) {
                        div {
                            css {
                                gridColumn = ident("1 / 3")
                            }
                            if (d3Array != null) {
                                h3 { +"Cycle Time Quantiles" }
                                div {
                                    css {
                                        display = Display.grid
                                        gridTemplateColumns = web.cssom.repeat(5, 1.fr)
                                    }
                                    div {
                                        h4 { +"90%" }
                                        +"${cycleTimes.quantile(0.9, d3Array)}"
                                    }
                                    div {
                                        h4 { +"75%" }
                                        +"${cycleTimes.quantile(0.75, d3Array)}"
                                    }
                                    div {
                                        h4 { +"50% (Median)" }
                                        +"${cycleTimes.quantile(0.5, d3Array)}"
                                    }
                                    div {
                                        h4 { +"25%" }
                                        +"${cycleTimes.quantile(0.25, d3Array)}"
                                    }
                                    div {
                                        h4 { +"10%" }
                                        +"${cycleTimes.quantile(0.25, d3Array)}"
                                    }
                                }
                            }
                        }
                    }
                }
            }
            div {
                css { fontSize = 0.75.em }
                filteredContributions.forEach { contribution ->
                    ContributionCard(contribution = contribution, contributors = contributors, key = contribution.id)
                }
            }
        }
    }
}

private fun List<Duration>.quantile(p: Double, d3Array: D3Array): Duration = d3Array.quantileSorted(
    map { it.toDouble(DurationUnit.MILLISECONDS) }.toTypedArray(),
    p,
).milliseconds
