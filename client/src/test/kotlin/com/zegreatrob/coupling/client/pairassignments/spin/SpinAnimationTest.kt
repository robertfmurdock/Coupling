package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.components.pairassignments.AssignedPair
import com.zegreatrob.coupling.components.pairassignments.assignedPair
import com.zegreatrob.coupling.components.pairassignments.spin.AssignedPlayer
import com.zegreatrob.coupling.components.pairassignments.spin.ShowPlayer
import com.zegreatrob.coupling.components.pairassignments.spin.Shuffle
import com.zegreatrob.coupling.components.pairassignments.spin.SpinAnimationPanel
import com.zegreatrob.coupling.components.pairassignments.spin.SpinAnimationState
import com.zegreatrob.coupling.components.pairassignments.spin.Start
import com.zegreatrob.coupling.components.pairassignments.spin.pairAssignmentStyles
import com.zegreatrob.coupling.components.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.components.pairassignments.spin.playerRosterStyles
import com.zegreatrob.coupling.components.pairassignments.spin.playerSpotlightStyles
import com.zegreatrob.coupling.components.playerCard
import com.zegreatrob.coupling.components.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.findByClass
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

@Suppress("unused")
class SpinAnimationTest {

    class GivenOnePlayerAndOnePair {
        open class Setup {
            val player = stubPlayer()
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(player).withPins(emptySet()),
                ),
            )
        }

        @Test
        fun startWillMoveToShowFirstAssignedPlayer() = setup(object : Setup() {
            val state = Start
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(player))
        }
    }

    class GivenThreePlayersAndOnePair {
        open class Setup {
            val party = stubParty()
            val excludedPlayer = stubPlayer()
            val players = listOf(
                stubPlayer(),
                excludedPlayer,
                stubPlayer(),
            )
            private val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(players[0], players[2]).withPins(emptySet()),
                ),
            )
            val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersExceptExcluded() = setup(object : Setup() {
            val state = Start
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players - excludedPlayer)
            }
        }
    }

    class GivenFourPlayersAndTwoPairs {
        open class Setup {
            val party = stubParty()
            val players = listOf(
                stubPlayer(),
                stubPlayer(),
                stubPlayer(),
                stubPlayer(),
            )
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(players[1], players[3]).withPins(emptySet()),
                    pairOf(players[0], players[2]).withPins(emptySet()),
                ),
            )

            val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(object : Setup() {
            val state = Start
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players)
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                    ),
                )
            }
        }

        @Test
        fun startWillMoveToShuffleStep1() = setup(object : Setup() {
            val state = Start
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(target = pairAssignments.pairs[0].players[0].player, step = 0))
        }

        @Test
        fun startWillEventuallyMoveToShowFirstPlayer() = setup(object : Setup() {
            val state = Start

            val maxTries = 100
            var tries = 0
            fun SpinAnimationState.nextUntilNotShuffle(): SpinAnimationState? = next(pairAssignments)
                .also { tries++ }
                .let { next ->
                    if (next is Shuffle && tries < maxTries) {
                        next.nextUntilNotShuffle()
                    } else {
                        next
                    }
                }
        }) exercise {
            state.nextUntilNotShuffle()
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(pairAssignments.pairs[0].players[0].player))
        }

        @Test
        fun whenShowingFirstPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = players[1]
            val state = ShowPlayer(firstAssignedPlayer)
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(firstAssignedPlayer)
            result.playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
            result.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                    pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                ),
            )
        }

        @Test
        fun whenShowingMidwayShownPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayShownPlayer = pairAssignments.pairs[1].players[0].player
            val state = ShowPlayer(midwayShownPlayer)
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify {
            it.playerInSpotlight().assertIsEqualTo(midwayShownPlayer)
            it.playersInRoster().assertIsEqualTo(
                listOf(pairAssignments.pairs[1].players[1].player),
            )
            it.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairAssignments.pairs[0],
                    pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                ),
            )
        }

        @Test
        fun showPlayerWillTransitionToAssigned() = setup(object : Setup() {
            val state = ShowPlayer(pairAssignments.pairs[0].players[0].player)
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(AssignedPlayer(pairAssignments.pairs[0].players[0].player))
        }

        @Test
        fun whenShowingFirstAssignedPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = pairAssignments.pairs[0].players[0].player
            val state = AssignedPlayer(firstAssignedPlayer)
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(placeholderPlayer)
            result.playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
            result.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairOf(firstAssignedPlayer, placeholderPlayer).withPins(emptySet()),
                    pairOf(placeholderPlayer, placeholderPlayer).withPins(emptySet()),
                ),
            )
        }

        @Test
        fun whenShowingMidwayAssignedPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayAssignedPlayer = pairAssignments.pairs[1].players[0].player
            val state = AssignedPlayer(midwayAssignedPlayer)
        }) exercise {
            shallow(SpinAnimationPanel(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(placeholderPlayer)
            result.playersInRoster().assertIsEqualTo(
                listOf(pairAssignments.pairs[1].players[1].player),
            )
            result.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairAssignments.pairs[0],
                    pairOf(midwayAssignedPlayer, placeholderPlayer).withPins(emptySet()),
                ),
            )
        }

        @Test
        fun assignedPlayerWillTransitionToShuffleNextPlayerInPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[0].player)
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(pairAssignments.pairs[0].players[1].player, 0))
        }

        @Test
        fun assignedPlayerWillTransitionToShuffleNextPlayerInNextPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[1].player)
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(pairAssignments.pairs[1].players[0].player, 0))
        }
    }

    companion object {

        private fun ShallowWrapper<dynamic>.playersInRoster() = findByClass(playerRosterStyles.toString())
            .find(playerCard)
            .map { it.dataprops<PlayerCard>().player }
            .toList()

        private fun ShallowWrapper<dynamic>.playerInSpotlight() = findByClass("$playerSpotlightStyles")
            .find(playerCard).run {
                if (length == 1) dataprops<PlayerCard>().player else null
            }

        private fun ShallowWrapper<dynamic>.shownPairAssignments() = findByClass("$pairAssignmentStyles")
            .find(assignedPair)
            .map { it.dataprops<AssignedPair>().pair }
            .toList()
    }
}
