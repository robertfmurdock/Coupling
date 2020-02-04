package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
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
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.visibility
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.key
import styled.css
import styled.styledDiv

data class SpinAnimationProps(
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val state: SpinAnimationState
) : RProps

val placeholderPlayer = Player("?", name = "Next...")

object SpinAnimation : FRComponent<SpinAnimationProps>(provider()) {

    private val styles = useStyles("pairassignments/SpinAnimation")

    fun RBuilder.spinAnimation(
        players: List<Player>,
        pairAssignments: PairAssignmentDocument,
        state: SpinAnimationState
    ) = child(SpinAnimation.component.rFunction, SpinAnimationProps(players, pairAssignments, state))

    override fun render(props: SpinAnimationProps): ReactElement {
        val state = props.state
        val pairAssignments = props.pairAssignments
        val orderedPairedPlayers = pairAssignments.orderedPairedPlayers()
        val players = props.players.filter(orderedPairedPlayers::contains)

        val rosterPlayers = when (state) {
            Start -> players
            is ShowPlayer -> players - pairAssignments.presentedPlayers(state.player, true)
            is AssignedPlayer -> players - pairAssignments.presentedPlayers(state.player, true)
            End -> emptyList()
        }

        val revealedPairs = when (state) {
            Start -> pairAssignments.makePlaceholderPlayers().toSimulatedPairs()
            is AssignedPlayer -> pairAssignments.revealedPairs(state.player, true)
            is ShowPlayer -> pairAssignments.revealedPairs(state.player, false)
            End -> emptyList()
        }

        return reactElement {
            flipper(flipKey = state.toString(), classes = styles.className) {
                playerRoster(rosterPlayers)
                div(classes = styles["playerSpotlight"]) {
                    when (state) {
                        is ShowPlayer -> flippedPlayer(state.player)
                        else -> placeholderPlayerCard()
                    }
                }
                assignedPairs(revealedPairs)
            }
        }
    }

    private fun RBuilder.placeholderPlayerCard() = styledDiv {
        css { visibility = Visibility.hidden }
        flippedPlayer(placeholderPlayer)
    }

    private fun PairAssignmentDocument.revealedPairs(player: Player, inclusive: Boolean) =
        this.presentedPlayers(player, inclusive)
            .let { it + makePlaceholderPlayers(it, this) }.toSimulatedPairs()

    private fun List<Player>.toSimulatedPairs() = chunked(2)
        .map { if (it.size > 1) pairOf(it[0], it[1]) else pairOf(it[0]) }
        .map { it.withPins(emptyList()) }

    private fun makePlaceholderPlayers(it: List<Player>, document: PairAssignmentDocument) =
        generateSequence { placeholderPlayer }.take(document.orderedPairedPlayers().size - it.size)
            .toList()

    private fun PairAssignmentDocument.makePlaceholderPlayers() =
        generateSequence { placeholderPlayer }.take(orderedPairedPlayers().size)
            .toList()

    private fun PairAssignmentDocument.presentedPlayers(player: Player, inclusive: Boolean = false): List<Player> {
        val orderedPairedPlayers = orderedPairedPlayers()
        val index = orderedPairedPlayers.indexOf(player)
        val toIndex = if (inclusive) index + 1 else index
        return orderedPairedPlayers.subList(0, toIndex)
    }

    private fun PairAssignmentDocument.orderedPairedPlayers() = pairs
        .flatMap { it.players }
        .map { it.player }

    private fun RBuilder.flippedPlayer(player: Player, key: String? = null) = flipped(player.id ?: "") {
        styledDiv {
            attrs { this.key = key ?: "" }
            css { display = Display.inlineBlock }
            playerCard(PlayerCardProps(TribeId(""), player))
        }
    }

    private fun RBuilder.assignedPairs(revealedPairs: List<PinnedCouplingPair>) =
        div(classes = styles["pairAssignments"]) {
            revealedPairs.mapIndexed { index, it ->
                assignedPair(
                    Tribe(TribeId("")),
                    it,
                    { _, _, _ -> },
                    { _, _ -> },
                    false,
                    {},
                    key = "$index"
                )
            }
        }

    private fun RBuilder.playerRoster(players: List<Player>) = div(classes = styles["playerRoster"]) {
        players.map { flippedPlayer(it, key = it.id) }
    }
}

sealed class SpinAnimationState {
    abstract fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState
}

object Start : SpinAnimationState() {
    override fun toString() = "Start"
    override fun next(pairAssignments: PairAssignmentDocument) = ShowPlayer(pairAssignments.pairs[0].players[0].player)
}

object End : SpinAnimationState() {
    override fun toString() = "End"
    override fun next(pairAssignments: PairAssignmentDocument) = this
}

data class ShowPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument) = AssignedPlayer(player)
}

data class AssignedPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val orderedPlayers = pairAssignments.pairs.flatMap { it.players }.map { it.player }
        val playerIndex = orderedPlayers.indexOf(player)
        val nextPlayer = orderedPlayers.getOrNull(playerIndex + 1)
        return nextPlayer?.let(::ShowPlayer) ?: End
    }
}

data class AnimatorProps(val players: List<Player>, val pairAssignments: PairAssignmentDocument) : RProps

object Animator : FRComponent<AnimatorProps>(provider()), WindowFunctions {

    fun RBuilder.animator(players: List<Player>, pairAssignments: PairAssignmentDocument) =
        child(Animator.component.rFunction, AnimatorProps(players, pairAssignments))

    override fun render(props: AnimatorProps) = reactElement {
        val (state, setState) = useState<SpinAnimationState>(Start)
        val (players, pairAssignments) = props

        useEffect {
            window.setTimeout({
                setState(state.next(pairAssignments))
            }, 1000)
        }
        spinAnimation(players, pairAssignments, state)
    }
}
