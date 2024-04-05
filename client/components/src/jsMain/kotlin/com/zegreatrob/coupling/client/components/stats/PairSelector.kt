package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState
import web.html.InputType

external interface PairSelectorProps : Props {
    var pairs: List<CouplingPair>
    var onSelectionChange: (List<CouplingPair>) -> Unit
}

@ReactFunc
val PairSelector by nfc<PairSelectorProps> { props ->
    val pairs = props.pairs
    val onSelectionChange = props.onSelectionChange

    val (selectedPairs, setSelectedPairs) = useState<Set<CouplingPair>>(emptySet())

    pairs.forEach { pair: CouplingPair ->
        label {
            +pair.asArray().joinToString("-", transform = Player::name)
            input {
                type = InputType.checkbox
                value = pair.pairId
                onChange = {
                    val newSelections = if (selectedPairs.contains(pair)) {
                        selectedPairs - setOf(pair)
                    } else {
                        selectedPairs + listOf(pair)
                    }
                    setSelectedPairs(newSelections)
                    onSelectionChange(pairs.filter(newSelections::contains))
                }
            }
        }
    }
}
