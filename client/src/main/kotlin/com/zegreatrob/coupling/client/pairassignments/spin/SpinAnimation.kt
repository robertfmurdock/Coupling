package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.RProps
import react.dom.div

object SpinAnimation : FRComponent<SpinAnimationProps>(provider()) {

    private val styles = useStyles("pairassignments/SpinAnimation")

    override fun render(props: SpinAnimationProps) = with(props) {

        val rosterPlayers = when (state) {
            is ShowPlayer -> players - state.player
            Start -> players
        }

        reactElement {
            div(classes = styles.className) {
                playerRoster(rosterPlayers)
                when (state) {
                    is ShowPlayer -> div(classes = styles["playerSpotlight"]) {
                        playerCard(PlayerCardProps(TribeId(""), state.player))
                    }
                }


            }
        }
    }

    private fun RBuilder.playerRoster(players: List<Player>) = div(classes = styles["playerRoster"]) {
        players.map { playerCard(PlayerCardProps(TribeId(""), it), key = it.id) }
    }
}

data class SpinAnimationProps(
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val state: SpinAnimationState
) : RProps

sealed class SpinAnimationState

object Start : SpinAnimationState()

data class ShowPlayer(val player: Player) : SpinAnimationState()
