package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.cssom.deg
import web.html.InputType

external interface PairSelectorProps : Props {
    var pairs: List<CouplingPair>
    var selectedPairs: List<CouplingPair>
    var onSelectionChange: (List<CouplingPair>) -> Unit
}

@ReactFunc
val PairSelector by nfc<PairSelectorProps> { props ->
    val pairs = props.pairs
    val selectedPairs = props.selectedPairs
    val onSelectionChange = props.onSelectionChange
    div {
        pairs.forEach { pair: CouplingPair ->
            div {
                label {
                    ariaLabel = pair.pairName
                    pair.forEach {
                        PlayerCard(it, size = 25, tilt = 0.deg)
                    }
                    input {
                        type = InputType.checkbox
                        value = pair.pairId
                        onChange = {
                            val newSelections = if (selectedPairs.contains(pair)) {
                                selectedPairs - setOf(pair)
                            } else {
                                selectedPairs + listOf(pair)
                            }
                            onSelectionChange(pairs.filter(newSelections::contains))
                        }
                    }
                }
            }
        }
    }
}
