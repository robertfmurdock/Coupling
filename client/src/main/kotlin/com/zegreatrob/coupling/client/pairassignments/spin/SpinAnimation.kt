package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.AssignedPair.assignedPair
import com.zegreatrob.coupling.client.pairassignments.spin.SpinAnimation.spinAnimation
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.RProps
import react.dom.div

data class SpinAnimationProps(
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val state: SpinAnimationState
) : RProps


object SpinAnimation : FRComponent<SpinAnimationProps>(provider()) {

    private val styles = useStyles("pairassignments/SpinAnimation")

    fun RBuilder.spinAnimation(
        players: List<Player>,
        pairAssignments: PairAssignmentDocument,
        state: SpinAnimationState
    ) = child(SpinAnimation.component.rFunction, SpinAnimationProps(players, pairAssignments, state))

    override fun render(props: SpinAnimationProps) = with(props) {
        val rosterPlayers = when (state) {
            is ShowPlayer -> players - state.player
            is AssignedPlayer -> players - state.player
            Start -> players
            End -> emptyList()
        }

        val revealedPairs = when (state) {
            is AssignedPlayer -> listOf(pairOf(state.player).withPins(emptyList()))
            is ShowPlayer -> emptyList()
            Start -> emptyList()
            End -> emptyList()
        }

        reactElement {
            div(classes = styles.className) {
                playerRoster(rosterPlayers)
                when (state) {
                    is ShowPlayer -> div(classes = styles["playerSpotlight"]) {
                        playerCard(PlayerCardProps(TribeId(""), state.player))
                    }
                }
                assignedPairs(revealedPairs)
            }
        }
    }

    private fun RBuilder.assignedPairs(revealedPairs: List<PinnedCouplingPair>) =
        div(classes = styles["pairAssignments"]) {
            revealedPairs.map {
                assignedPair(
                    Tribe(TribeId("")),
                    it,
                    { _, _, _ -> },
                    { _, _ -> },
                    false,
                    {},
                    it.players.joinToString { player -> player.player.id ?: "" })
            }
        }

    private fun RBuilder.playerRoster(players: List<Player>) = div(classes = styles["playerRoster"]) {
        players.map { playerCard(PlayerCardProps(TribeId(""), it), key = it.id) }
    }
}

sealed class SpinAnimationState {
    abstract fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState
}

object Start : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument) = ShowPlayer(pairAssignments.pairs[0].players[0].player)
}

object End : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument) = this
}

data class ShowPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument) = AssignedPlayer(player)
}

data class AssignedPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState = End
}

data class AnimatorProps(val players: List<Player>, val pairAssignments: PairAssignmentDocument) : RProps

object Animator : FRComponent<AnimatorProps>(provider()) {

    fun RBuilder.animator(players: List<Player>, pairAssignments: PairAssignmentDocument) =
        child(Animator.component.rFunction, AnimatorProps(players, pairAssignments))

    override fun render(props: AnimatorProps) = reactElement {
        val (state, setState) = useState<SpinAnimationState>(Start)
        val (players, pairAssignments) = props

        useEffect { setState(state.next(pairAssignments)) }
        spinAnimation(players, pairAssignments, state)
    }
}
