package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.PlayerCardRenderer
import com.zegreatrob.coupling.client.rFunction
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.client.useState
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import loadStyles
import react.RProps
import react.dom.a
import react.dom.div

external fun encodeURIComponent(input: String?)

interface PrepareSpinRenderer : PlayerCardRenderer {

    companion object {
        private val styles: PrepareSpinStyles = loadStyles("PrepareSpin")
    }

    val prepareSpin
        get() = rFunction<PrepareSpinProps> { props ->
            val (tribe, players, history, pathSetter) = props

            val (playerSelections, setPlayerSelections) = useState(
                    players.map { it to isInLastSetOfPairs(it, history) }
            )

            div(classes = "react-prepare-spin") {
                attrs { classes += styles.className }

                div {
                    element(tribeBrowser, TribeBrowserProps(tribe, pathSetter))
                }
                div {
                    div {
                        a(classes = "spin-button super pink button") {
                            attrs {
                                classes += styles.spinButton
                                onClickFunction = { goToNewPairAssignments(pathSetter, tribe, playerSelections) }
                            }
                            +"Spin!"
                        }
                    }
                    playerSelections.map { (player, isSelected) ->
                        element(playerCard, PlayerCardProps(
                                tribe.id,
                                player,
                                pathSetter,
                                true,
                                className = styles.playerCard + if (isSelected) "" else " disabled",
                                onClick = {
                                    setPlayerSelections(
                                            flipSelectionForPlayer(player, isSelected, playerSelections)
                                    )
                                }
                        ))
                    }
                }
            }
        }

    private fun flipSelectionForPlayer(
            targetPlayer: Player,
            targetIsSelected: Boolean,
            playerSelections: List<Pair<Player, Boolean>>
    ) = playerSelections.map { pair ->
        if (pair.first == targetPlayer) {
            Pair(targetPlayer, !targetIsSelected)
        } else {
            pair
        }
    }

    private fun goToNewPairAssignments(
            pathSetter: (String) -> Unit,
            tribe: KtTribe,
            playerSelections: List<Pair<Player, Boolean>>
    ) = pathSetter(
            "/${tribe.id.value}/pairAssignments/new?${playerSelections.buildQueryParameters()}"
    )

    private fun List<Pair<Player, Boolean>>.buildQueryParameters() = filter { (_, isSelected) -> isSelected }
            .joinToString("&") { (player, _) ->
                "player=${encodeURIComponent(player.id)}"
            }

    private fun isInLastSetOfPairs(player: Player, history: List<PairAssignmentDocument>) = if (history.isEmpty()) {
        true
    } else {
        history.first()
                .pairs.map { it.players }
                .flatten()
                .map { it.player.id }
                .contains(player.id)
    }
}

external interface PrepareSpinStyles {
    val className: String
    val spinButton: String
    val playerCard: String
}

data class PrepareSpinProps(
        val tribe: KtTribe,
        val players: List<Player>,
        val history: List<PairAssignmentDocument>,
        val pathSetter: (String) -> Unit
) : RProps