package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.graphing.external.d3.array.quantileSorted
import com.zegreatrob.coupling.client.components.stats.ContributionControlPanelFrame
import com.zegreatrob.coupling.client.components.stats.ContributionLabelFilter
import com.zegreatrob.coupling.client.components.stats.EnumSelector
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import popper.core.ReferenceElement
import react.Key
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h4
import react.useState
import web.cssom.Display
import web.cssom.em
import web.cssom.fr
import web.cssom.ident
import web.html.HTMLElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

external interface ContributionListContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
    var window: ContributionWindow
    var setWindow: (ContributionWindow) -> Unit
    var players: List<Player>
    var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>
}

@ReactFunc
val ContributionListContent by nfc<ContributionListContentProps> { props ->
    val (party, contributions, window, setWindow, players, dispatchFunc) = props
    val allLabels = contributions.mapNotNull(Contribution::label).toSet()
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    val filteredContributions = selectedLabelFilter?.let { contributions.filter { it.label == selectedLabelFilter } }
        ?: contributions
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

                            h3 { +"Cycle Time Quantiles" }
                            div {
                                css {
                                    display = Display.grid
                                    gridTemplateColumns = web.cssom.repeat(5, 1.fr)
                                }
                                div {
                                    h4 { +"90%" }
                                    +"${cycleTimes.quantile(0.9)}"
                                }
                                div {
                                    h4 { +"75%" }
                                    +"${cycleTimes.quantile(0.75)}"
                                }
                                div {
                                    h4 { +"50% (Median)" }
                                    +"${cycleTimes.quantile(0.5)}"
                                }
                                div {
                                    h4 { +"25%" }
                                    +"${cycleTimes.quantile(0.25)}"
                                }
                                div {
                                    h4 { +"10%" }
                                    +"${cycleTimes.quantile(0.10)}"
                                }
                            }
                        }
                    }
                }
            }
            div {
                css { fontSize = 0.75.em }
                ContributionPopUpMenu(party.id, players, props.dispatchFunc) { setSelectedPlayerCard ->
                    val onPlayerClick = { player: Player, element: HTMLElement ->
                        setSelectedPlayerCard(ReferenceElement(element)!!, player)
                    }
                    filteredContributions.forEach { contribution ->
                        ContributionCard(
                            key = Key(contribution.id.value.toString()),
                            partyId = party.id,
                            contribution = contribution,
                            players = props.players,
                            onPlayerClick = onPlayerClick,
                        )
                    }
                }
            }
        }
    }
}

private fun List<Duration>.quantile(p: Double): Duration = quantileSorted(
    map { it.toDouble(DurationUnit.MILLISECONDS) }
        .sorted()
        .toTypedArray(),
    p,
).milliseconds
