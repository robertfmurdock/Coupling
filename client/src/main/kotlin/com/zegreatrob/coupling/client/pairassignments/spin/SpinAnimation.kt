package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.AssignedPair.assignedPair
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsHeader.pairAssignmentsHeader
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
import react.RHandler
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.key
import styled.css
import styled.styledDiv

data class SpinAnimationProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument,
    val state: SpinAnimationState
) : RProps

val placeholderPlayer = Player("?", name = "Next...", callSignAdjective = "--------", callSignNoun = "--------")

data class SpinStateData(
    val rosterPlayers: List<Player>,
    val revealedPairs: List<PinnedCouplingPair>,
    val shownPlayer: Player?
)

object SpinAnimation : FRComponent<SpinAnimationProps>(provider()) {

    private val styles = useStyles("pairassignments/SpinAnimation")

    fun RBuilder.spinAnimation(
        tribe: Tribe,
        players: List<Player>,
        pairAssignments: PairAssignmentDocument,
        state: SpinAnimationState
    ) = child(SpinAnimation.component.rFunction, SpinAnimationProps(tribe, players, pairAssignments, state))

    override fun render(props: SpinAnimationProps): ReactElement {
        val state = props.state
        val tribe = props.tribe
        val pairAssignments = props.pairAssignments
        val orderedPairedPlayers = pairAssignments.orderedPairedPlayers()
        val players = props.players.filter(orderedPairedPlayers::contains)

        val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)

        return reactElement {
            div(classes = styles.className) {
                pairAssignmentsHeader(pairAssignments)
                assignedPairs(tribe, revealedPairs)
                playerSpotlight(shownPlayer)
                playerRoster(rosterPlayers)
            }
        }
    }

    private fun RBuilder.playerSpotlight(shownPlayer: Player?) = div(classes = styles["playerSpotlight"]) {
        if (shownPlayer != null)
            flippedPlayer(shownPlayer)
        else
            placeholderPlayerCard()
    }

    private fun RBuilder.placeholderPlayerCard() = styledDiv {
        css { visibility = Visibility.hidden; display = Display.inlineBlock }
        flippedPlayer(placeholderPlayer)
    }

    private fun RBuilder.flippedPlayer(player: Player, key: String? = null) = flipped(player.id ?: "") {
        styledDiv {
            attrs { this.key = key ?: "" }
            css { display = Display.inlineBlock }
            playerCard(PlayerCardProps(TribeId(""), player))
        }
    }

    private fun RBuilder.assignedPairs(tribe: Tribe, revealedPairs: List<PinnedCouplingPair>) =
        div(classes = styles["pairAssignments"]) {
            revealedPairs.mapIndexed { index, it ->
                assignedPair(
                    tribe,
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
        players.map {
            if (it == placeholderPlayer) {
                placeholderPlayerCard()
            } else {
                flippedPlayer(it, key = it.id)
            }
        }
    }
}

private fun PairAssignmentDocument.orderedPairedPlayers() = pairs
    .asSequence()
    .flatMap { it.players.asSequence() }
    .map { it.player }

sealed class SpinAnimationState {
    abstract fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState
    abstract fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData
    open fun getDuration(pairAssignments: PairAssignmentDocument): Int = 200
}

object Start : SpinAnimationState() {
    override fun toString() = "Start"
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val orderedPairedPlayers = pairAssignments.orderedPairedPlayers()
        val numberOfPlayersShuffling = orderedPairedPlayers.count()
        val firstPlayer = orderedPairedPlayers.first()
        return if (numberOfPlayersShuffling == 1) {
            ShowPlayer(firstPlayer)
        } else {
            Shuffle(firstPlayer, 0)
        }
    }

    override fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument) = SpinStateData(
        rosterPlayers = allPlayers,
        revealedPairs = makePlaceholderPlayers(pairAssignments).toSimulatedPairs(),
        shownPlayer = null
    )
}

object End : SpinAnimationState() {
    override fun toString() = "End"
    override fun next(pairAssignments: PairAssignmentDocument) = this
    override fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument) = SpinStateData(
        rosterPlayers = emptyList(),
        revealedPairs = emptyList(),
        shownPlayer = null
    )
}

data class ShowPlayer(val player: Player) : SpinAnimationState() {
    override fun getDuration(pairAssignments: PairAssignmentDocument) = 500
    override fun next(pairAssignments: PairAssignmentDocument) = AssignedPlayer(player)

    override fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        fun ifEmptyAddPlaceholder(rosterPlayers: List<Player>) = if (rosterPlayers.isEmpty())
            makePlaceholderPlayers(pairAssignments)
        else
            rosterPlayers

        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(player)

        return SpinStateData(
            rosterPlayers = (allPlayers - presentedPlayers - player).let(::ifEmptyAddPlaceholder),
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = player
        )
    }
}

data class Shuffle(val target: Player, val step: Int) : SpinAnimationState() {

    private val fullShuffles = 2
    private val shuffleTotalDuration = 1000

    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val numberOfPlayersShuffling = numberOfPlayersShuffling(pairAssignments)
        val hasShuffledEnough = step / numberOfPlayersShuffling >= fullShuffles
        return if (numberOfPlayersShuffling == 1 || hasShuffledEnough) {
            ShowPlayer(target)
        } else {
            Shuffle(target, step + 1)
        }
    }

    private fun numberOfPlayersShuffling(pairAssignments: PairAssignmentDocument): Int {
        val orderedPairedPlayers = pairAssignments.orderedPairedPlayers()

        val indexOfTarget = orderedPairedPlayers.indexOf(target)

        return orderedPairedPlayers.count() - indexOfTarget
    }

    override fun getDuration(pairAssignments: PairAssignmentDocument) =
        shuffleTotalDuration / (numberOfPlayersShuffling(pairAssignments) * fullShuffles)

    override fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        fun rotateList(rosterPlayers: List<Player>): List<Player> {
            val peopleToRotate = step % rosterPlayers.size
            return rosterPlayers.takeLast(rosterPlayers.size - peopleToRotate) + rosterPlayers.take(peopleToRotate)
        }

        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(target)
        return SpinStateData(
            rosterPlayers = (allPlayers - presentedPlayers).let(::rotateList),
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = null
        )

    }

}

data class AssignedPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val orderedPlayers = pairAssignments.pairs.flatMap { it.players }.map { it.player }
        val playerIndex = orderedPlayers.indexOf(player)
        val nextPlayer = orderedPlayers.getOrNull(playerIndex + 1)
        return nextPlayer?.let { Shuffle(it, 0) } ?: End
    }

    override fun stateData(allPlayers: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(player) + player
        return SpinStateData(
            rosterPlayers = allPlayers - presentedPlayers,
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = null
        )
    }

}

private fun PairAssignmentDocument.previouslyPresentedPlayers(player: Player) = orderedPairedPlayers()
    .takeWhile { it != player }
    .toList()

private fun PairAssignmentDocument.revealedPairs(presentedPlayers: List<Player>) =
    presentedPlayers
        .let { it + makePlaceholderPlayers(it, this) }.toSimulatedPairs()

private fun List<Player>.toSimulatedPairs() = chunked(2)
    .map { if (it.size > 1) pairOf(it[0], it[1]) else pairOf(it[0]) }
    .map { it.withPins(emptyList()) }

private fun makePlaceholderPlayers(it: List<Player>, document: PairAssignmentDocument) = infinitePlaceholders()
    .take(document.orderedPairedPlayers().count() - it.size)
    .toList()

private fun makePlaceholderPlayers(pairAssignmentDocument: PairAssignmentDocument) = infinitePlaceholders()
    .take(pairAssignmentDocument.orderedPairedPlayers().count())
    .toList()

private fun infinitePlaceholders() = generateSequence { placeholderPlayer }

data class AnimatorProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val enabled: Boolean
) : RProps

object Animator : FRComponent<AnimatorProps>(provider()), WindowFunctions {

    private val animationContextConsumer = animationsDisabledContext.Consumer

    fun RBuilder.animator(
        tribe: Tribe,
        players: List<Player>,
        pairAssignments: PairAssignmentDocument?,
        enabled: Boolean,
        handler: RHandler<AnimatorProps>
    ) = child(Animator.component.rFunction, AnimatorProps(tribe, players, pairAssignments, enabled), handler = handler)

    override fun render(props: AnimatorProps) = reactElement {
        val (tribe, players, pairAssignments, enabled) = props
        val (state, setState) = useState<SpinAnimationState>(Start)

        useEffect {
            if (state != End)
                window.setTimeout({ setState(nextState(pairAssignments, state)) }, getDuration(state, pairAssignments))
        }
        consumer(animationContextConsumer) { animationsDisabled: Boolean ->
            if (!animationsDisabled && enabled && pairAssignments != null && pairAssignments.id == null) {
                flipper(flipKey = state.toString()) {
                    if (state == End)
                        props.children()
                    else
                        spinAnimation(tribe, players, pairAssignments, state)
                }
            } else {
                props.children()
            }
        }
    }

    private fun getDuration(state: SpinAnimationState, pairAssignments: PairAssignmentDocument?) =
        if (pairAssignments == null)
            0
        else
            state.getDuration(pairAssignments)

    private fun nextState(pairAssignments: PairAssignmentDocument?, state: SpinAnimationState) =
        if (pairAssignments == null)
            End
        else
            state.next(pairAssignments)
}
