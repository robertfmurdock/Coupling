package com.zegreatrob.coupling.client.components.stats

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
import react.useMemo
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px
import web.html.InputType

external interface PairFrequencyControlsProps : Props {
    var pairsContributions: List<Pair<CouplingPair, List<Contribution>>>
    var view: (List<Pair<CouplingPair, List<Contribution>>>) -> ReactNode
}

@ReactFunc
val PairFrequencyControls by nfc<PairFrequencyControlsProps> { (pairsContributions, view) ->
    val (shouldFake, setShouldFake) = useState(false)
    val (selectedPairs, setSelectedPairs) = useState(emptyList<CouplingPair>())
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)

    val fakeContributions = useMemo { pairsContributions.map { it.first to generateFakeContributions() } }

    val allPairContributions: List<Pair<CouplingPair, List<Contribution>>> = if (shouldFake) {
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
            select {
                onChange = { event ->
                    setSelectedLabelFilter(event.target.value.let { if (it == "NULL") null else it })
                }
                option {
                    value = "NULL"
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
            css {
                display = Display.inlineFlex
                marginLeft = 20.px
            }
            div {
                css { display = Display.inlineBlock }
                label {
                    ariaLabel = "Fake the data"
                    +"Fake the data"
                    input {
                        type = InputType.checkbox
                        value = shouldFake
                        onChange = { setShouldFake(!shouldFake) }
                    }
                }
                PairSelector(
                    pairs = allPairContributions.toMap()
                        .filterValues(List<Contribution>::isNotEmpty).keys.toList(),
                    onSelectionChange = setSelectedPairs::invoke,
                )
            }
            div {
                css {
                    display = Display.inlineBlock
                    width = 600.px
                    height = 600.px
                    backgroundColor = Color("white")
                }
                +view(filteredData)
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
