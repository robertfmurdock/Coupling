package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.contribution.contributionContentBackgroundColor
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import react.Fragment
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.useEffect
import react.useState
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.WhiteSpace
import web.cssom.fr
import web.cssom.px
import web.html.InputType

external interface PairFrequencyControlsProps : Props {
    var pairsContributions: List<Pair<CouplingPair, ContributionReport>>
    var view: (VisualizationContext) -> ReactNode
    var window: GqlContributionWindow
    var setWindow: (GqlContributionWindow) -> Unit
}

enum class Visualization {
    Heatmap,
    LineOverTime,
    CycleTimeBarChart,
}

enum class FakeDataStyle {
    RandomPairs,
    RandomPairsWithRandomSolos,
    StrongPairingTeam,
}

data class VisualizationContext(
    val visualization: Visualization,
    val data: List<Pair<CouplingPair, ContributionReport>>,
)

@ReactFunc
val PairFrequencyControls by nfc<PairFrequencyControlsProps> { (pairsContributions, view, selectedWindow, setWindow) ->
    val (fakeStyle, setFakeStyle) = useState<FakeDataStyle?>(null)
    val (visualization, setVisualization) = useState(Visualization.Heatmap)
    val (selectedPairs, setSelectedPairs) = useState(emptyList<CouplingPair>())
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    val (fakeContributions, setFakeContributions) = useState<List<Pair<CouplingPair, ContributionReport>>>(emptyList())
    useEffect(fakeStyle) {
        if (fakeStyle != null) {
            setFakeContributions(generateFakeContributions(pairsContributions, selectedWindow, fakeStyle))
        }
    }
    val allPairContributions: List<Pair<CouplingPair, ContributionReport>> = if (fakeStyle != null) {
        fakeContributions
    } else {
        pairsContributions
    }

    val allLabels =
        allPairContributions.mapNotNull { it.second.contributions?.elements?.map(Contribution::label) }
            .flatten()
            .toSet()
    val filteredData = allPairContributions.applyFilters(
        selectedPairs,
        selectedLabelFilter,
    )
    div {
        css {
            whiteSpace = WhiteSpace.nowrap
        }
        div {
            css {
                display = Display.inlineFlex
                marginLeft = 20.px
            }
            div {
                css { display = Display.inlineBlock }
                PairSelector(
                    pairs = allPairContributions.toMap()
                        .filterValues { it.contributions?.isNotEmpty() == true }
                        .keys
                        .toList(),
                    selectedPairs = selectedPairs,
                    onSelectionChange = setSelectedPairs::invoke,
                )
            }
            div {
                div {
                    div {
                        css {
                            margin = 6.px
                            display = Display.grid
                            fontSize = FontSize.smaller
                            gridTemplateColumns = web.cssom.repeat(2, 1.fr)
                            alignItems = AlignItems.baseline
                        }
                        div {
                            EnumSelector(
                                default = selectedWindow,
                                onChange = setWindow,
                                label = ReactNode("Time Window"),
                                backgroundColor = contributionContentBackgroundColor,
                            )
                        }
                        div {
                            EnumSelector(
                                default = Visualization.Heatmap,
                                onChange = setVisualization::invoke,
                                label = ReactNode("Visualization Style"),
                                backgroundColor = contributionContentBackgroundColor,
                            )
                        }
                        CouplingSelect {
                            label = ReactNode("Label Filter")
                            backgroundColor = contributionContentBackgroundColor
                            selectProps = jso {
                                disabled = allLabels.size <= 1
                                onChange = { event -> setSelectedLabelFilter(event.handlePlaceholder()) }
                            }
                            option {
                                value = NULL_PLACEHOLDER
                                +"All Labels"
                            }
                            allLabels.map { label ->
                                option {
                                    value = label
                                    +label
                                }
                            }
                        }
                        div {
                            div {
                                EnumSelector(
                                    backgroundColor = contributionContentBackgroundColor,
                                    label = Fragment.create {
                                        div {
                                            css {
                                                display = Display.inlineFlex
                                                alignItems = AlignItems.center
                                            }
                                            +"Fake the data"
                                            input {
                                                type = InputType.checkbox
                                                value = fakeStyle != null
                                                onChange = {
                                                    setFakeStyle(if (it.target.checked) FakeDataStyle.RandomPairs else null)
                                                }
                                            }
                                        }
                                    },
                                    default = FakeDataStyle.RandomPairs,
                                    onChange = setFakeStyle::invoke,
                                    selectProps = jso {
                                        disabled = fakeStyle == null
                                    },
                                )
                            }
                        }
                    }
                }
                div {
                    css {
                        display = Display.inlineBlock
                        width = 600.px
                        height = 600.px
                        backgroundColor = Color("white")
                        borderRadius = 150.px
                    }
                    +view(VisualizationContext(visualization, filteredData))
                }
            }
        }
    }
}

private fun List<Pair<CouplingPair, ContributionReport>>.applyFilters(
    selectedPairs: List<CouplingPair>,
    selectedLabelFilter: String?,
): List<Pair<CouplingPair, ContributionReport>> {
    val transforms = if (selectedLabelFilter != null) {
        listOf(selectedLabelTransform(selectedLabelFilter))
    } else {
        emptyList()
    }

    return transforms.fold(filter { selectedPairs.contains(it.first) }) { list, transform ->
        list.map(transform)
    }
}

private fun selectedLabelTransform(selectedLabelFilter: String) =
    { (pair, report): Pair<CouplingPair, ContributionReport> ->
        pair to report.run {
            copy(contributions = contributions?.filter { it.element.label == selectedLabelFilter })
        }
    }
