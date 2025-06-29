package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairName
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import web.cssom.Angle
import web.cssom.px
import web.html.InputType
import web.html.checkbox

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
        css { margin = 2.px }
        PairPanel {
            div {
                css {
                    padding = 4.px
                }
                div {
                    CouplingButton {
                        sizeRuleSet = small
                        onClick = { props.onSelectionChange(pairs) }
                        +"Select All"
                    }
                }
                div {
                    CouplingButton {
                        sizeRuleSet = small
                        onClick = { props.onSelectionChange(emptyList()) }
                        +"Select None"
                    }
                }
            }
        }

        pairs.forEach { pair: CouplingPair ->
            PairPanel {
                key = pair.pairId
                label {
                    ariaLabel = pair.pairName
                    TiltedPlayerList(playerList = pair) { tilt: Angle, player: Player ->
                        PlayerCard(player, tilt = tilt, size = 25, key = player.id.value.toString())
                    }
                    input {
                        type = InputType.checkbox
                        checked = props.selectedPairs.contains(pair)
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
