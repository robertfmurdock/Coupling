package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.PlayerCardRenderer
import com.zegreatrob.coupling.client.reactFunctionComponent
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.TribeBrowserRenderer.Companion.tribeBrowser
import com.zegreatrob.coupling.client.useState
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import loadStyles
import react.RProps
import react.dom.RDOMBuilder
import react.dom.a
import react.dom.div

external fun encodeURIComponent(input: String?)

interface PrepareSpinRenderer {

    companion object : PlayerCardRenderer {
        private val styles: PrepareSpinStyles = loadStyles("PrepareSpin")

        val prepareSpin = reactFunctionComponent { props: PrepareSpinProps ->
            val (tribe, players, history, pathSetter) = props
            val (playerSelections, setPlayerSelections) = useState(
                    players.map { it to isInLastSetOfPairs(it, history) }
            )

            div(classes = styles.className) {
                div { component(tribeBrowser, TribeBrowserProps(tribe, pathSetter)) }
                div {
                    div { spinButton(tribe, playerSelections, pathSetter) }
                    selectablePlayerCardList(playerSelections, tribe, pathSetter, setPlayerSelections)
                }
            }
        }

        private fun RDOMBuilder<DIV>.spinButton(
                tribe: KtTribe,
                playerSelections: List<Pair<Player, Boolean>>,
                pathSetter: (String) -> Unit
        ) = a(classes = "super pink button") {
            attrs {
                classes += styles.spinButton
                onClickFunction = { goToNewPairAssignments(pathSetter, tribe, playerSelections) }
            }
            +"Spin!"
        }

        private fun RDOMBuilder<DIV>.selectablePlayerCardList(
                playerSelections: List<Pair<Player, Boolean>>,
                tribe: KtTribe,
                pathSetter: (String) -> Unit,
                setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit
        ) = playerSelections.map { (player, isSelected) ->
            playerCard(tribe, player, pathSetter, isSelected, setPlayerSelections, playerSelections)
        }

        private fun RDOMBuilder<DIV>.playerCard(tribe: KtTribe, player: Player, pathSetter: (String) -> Unit, isSelected: Boolean, setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit, playerSelections: List<Pair<Player, Boolean>>) {
            component(PlayerCardRenderer.playerCard, PlayerCardProps(
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
            ), null)
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