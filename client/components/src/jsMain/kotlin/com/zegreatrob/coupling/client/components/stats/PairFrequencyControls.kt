package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.ReactNode
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.useEffect
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px
import web.html.InputType

external interface PairFrequencyControlsProps : Props {
    var pairsContributions: List<Pair<CouplingPair, List<Contribution>>>
    var view: (VisualizationContext) -> ReactNode
    var window: JsonContributionWindow
    var setWindow: (JsonContributionWindow) -> Unit
}

enum class Visualization {
    Heatmap,
    LineOverTime,
}

enum class FakeDataStyle {
    RandomPairs,
    RandomPairsWithRandomSolos,
    StrongPairingTeam,
}

data class VisualizationContext(
    val visualization: Visualization,
    val data: List<Pair<CouplingPair, List<Contribution>>>,
)

@ReactFunc
val PairFrequencyControls by nfc<PairFrequencyControlsProps> { (pairsContributions, view, selectedWindow, setWindow) ->
    val (fakeStyle, setFakeStyle) = useState<FakeDataStyle?>(null)
    val (visualization, setVisualization) = useState(Visualization.Heatmap)
    val (selectedPairs, setSelectedPairs) = useState(emptyList<CouplingPair>())
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    val (fakeContributions, setFakeContributions) = useState<List<Pair<CouplingPair, List<Contribution>>>>(emptyList())
    useEffect(fakeStyle) {
        if (fakeStyle != null) {
            setFakeContributions(generateFakeContributions(pairsContributions, selectedWindow, fakeStyle))
        }
    }
    val allPairContributions: List<Pair<CouplingPair, List<Contribution>>> = if (fakeStyle != null) {
        fakeContributions
    } else {
        pairsContributions
    }

    val allLabels = allPairContributions.flatMap { it.second.map(Contribution::label) }.toSet()
    val filteredData = allPairContributions.applyFilters(
        selectedPairs,
        selectedLabelFilter,
    )
    div {
        css {
            whiteSpace = WhiteSpace.nowrap
        }
        div {
            EnumSelector(
                entries = JsonContributionWindow.entries,
                default = selectedWindow,
                setEnum = setWindow,
                valueOf = JsonContributionWindow::valueOf,
                enumName = JsonContributionWindow::name,
            )
            EnumSelector(
                entries = Visualization.entries,
                default = Visualization.Heatmap,
                setEnum = setVisualization::invoke,
                valueOf = Visualization::valueOf,
                enumName = Visualization::name,
            )
            select {
                onChange = { event -> setSelectedLabelFilter(event.handlePlaceholder()) }
                option {
                    value = NULL_PLACEHOLDER
                    +"No label filter"
                }
                allLabels.map { label ->
                    option {
                        value = label
                        +label
                    }
                }
            }
        }
        div {
            div {
                label {
                    ariaLabel = "Fake the data"
                    +"Fake the data"
                    input {
                        type = InputType.checkbox
                        value = fakeStyle != null
                        onChange = { setFakeStyle(if (it.target.checked) FakeDataStyle.RandomPairs else null) }
                    }
                }
            }
            if (fakeStyle != null) {
                div {
                    EnumSelector(
                        default = FakeDataStyle.RandomPairs,
                        entries = FakeDataStyle.entries,
                        setEnum = setFakeStyle::invoke,
                        valueOf = FakeDataStyle::valueOf,
                        enumName = FakeDataStyle::name,
                    )
                }
            }
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
                        .filterValues(List<Contribution>::isNotEmpty).keys.toList(),
                    selectedPairs = selectedPairs,
                    onSelectionChange = setSelectedPairs::invoke,
                )
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

private fun List<Pair<CouplingPair, List<Contribution>>>.applyFilters(
    selectedPairs: List<CouplingPair>,
    selectedLabelFilter: String?,
): List<Pair<CouplingPair, List<Contribution>>> {
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
    { (pair, contributions): Pair<CouplingPair, List<Contribution>> ->
        pair to contributions.filter { it.label == selectedLabelFilter }
    }
