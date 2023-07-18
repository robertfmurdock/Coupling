package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.client.components.Frame
import com.zegreatrob.coupling.model.flatMap
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.orderedPairedPlayers
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player

sealed class SpinAnimationState {
    abstract fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState?
    abstract fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData
    open fun duration(pairAssignments: PairAssignmentDocument): Int = 200

    companion object {
        fun sequence(pairAssignments: PairAssignmentDocument): Sequence<Frame<SpinAnimationState>> =
            generateSequence<Frame<SpinAnimationState>>(Frame(Start, 0)) { (state, time) ->
                state.next(pairAssignments)
                    ?.let { Frame(it, time + state.duration(pairAssignments)) }
            }
    }
}

data object Start : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val orderedPairedPlayers = pairAssignments.orderedPairedPlayers()
        val firstPlayer = orderedPairedPlayers.first()
        return if (orderedPairedPlayers.count() == 1) {
            ShowPlayer(firstPlayer)
        } else {
            Shuffle(firstPlayer, 0)
        }
    }

    override fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument) = SpinStateData(
        rosterPlayers = players,
        revealedPairs = makePlaceholderPlayers(pairAssignments).toSimulatedPairs(),
        shownPlayer = null,
    )
}

data object End : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState? = null
    override fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument) = SpinStateData(
        rosterPlayers = emptyList(),
        revealedPairs = emptyList(),
        shownPlayer = null,
    )
}

data class ShowPlayer(val player: Player) : SpinAnimationState() {
    override fun duration(pairAssignments: PairAssignmentDocument) = 500
    override fun next(pairAssignments: PairAssignmentDocument) =
        AssignedPlayer(player)

    override fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        fun ifEmptyAddPlaceholder(rosterPlayers: List<Player>) = rosterPlayers.ifEmpty {
            makePlaceholderPlayers(pairAssignments)
        }

        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(player)

        return SpinStateData(
            rosterPlayers = (players - presentedPlayers.toSet() - player).let(::ifEmptyAddPlaceholder),
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = player,
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

    override fun duration(pairAssignments: PairAssignmentDocument) =
        shuffleTotalDuration / (numberOfPlayersShuffling(pairAssignments) * fullShuffles)

    override fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        fun rotateList(rosterPlayers: List<Player>): List<Player> {
            val peopleToRotate = step % rosterPlayers.size
            return rosterPlayers.takeLast(rosterPlayers.size - peopleToRotate) + rosterPlayers.take(peopleToRotate)
        }

        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(target)
        return SpinStateData(
            rosterPlayers = (players - presentedPlayers.toSet()).let(::rotateList),
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = null,
        )
    }
}

data class AssignedPlayer(val player: Player) : SpinAnimationState() {
    override fun next(pairAssignments: PairAssignmentDocument): SpinAnimationState {
        val orderedPlayers = pairAssignments.pairs.flatMap(PinnedCouplingPair::players)
        val playerIndex = orderedPlayers.indexOf(player)
        val nextPlayer = orderedPlayers.getOrNull(playerIndex + 1)
        return nextPlayer?.let { Shuffle(it, 0) } ?: End
    }

    override fun stateData(players: List<Player>, pairAssignments: PairAssignmentDocument): SpinStateData {
        val presentedPlayers = pairAssignments.previouslyPresentedPlayers(player) + player
        return SpinStateData(
            rosterPlayers = players - presentedPlayers.toSet(),
            revealedPairs = pairAssignments.revealedPairs(presentedPlayers),
            shownPlayer = null,
        )
    }
}

private fun PairAssignmentDocument.previouslyPresentedPlayers(player: Player) = orderedPairedPlayers()
    .takeWhile { it != player }
    .toList()

private fun PairAssignmentDocument.revealedPairs(presentedPlayers: List<Player>) =
    presentedPlayers
        .let {
            it + makePlaceholderPlayers(
                it,
                this,
            )
        }.toSimulatedPairs()

private fun List<Player>.toSimulatedPairs() = chunked(2)
    .map {
        if (it.size > 1) {
            pairOf(
                it[0],
                it[1],
            )
        } else {
            pairOf(it[0])
        }
    }
    .map { it.withPins(emptySet()) }

private fun makePlaceholderPlayers(it: List<Player>, document: PairAssignmentDocument) = infinitePlaceholders()
    .take(document.orderedPairedPlayers().count() - it.size)
    .toList()

private fun makePlaceholderPlayers(pairAssignmentDocument: PairAssignmentDocument) = infinitePlaceholders()
    .take(pairAssignmentDocument.orderedPairedPlayers().count())
    .toList()

private fun infinitePlaceholders() = generateSequence { placeholderPlayer }
