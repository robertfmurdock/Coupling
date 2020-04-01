package com.zegreatrob.coupling.client.pairassignments.spin

import ShallowWrapper
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findByClass
import findComponent
import shallow
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import kotlin.test.Test

class SpinAnimationTest {

    class GivenOnePlayerAndOnePair {
        open class Setup {
            val player = stubPlayer()
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(player).withPins(emptyList())
                )
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
            val tribe = stubTribe()
            val excludedPlayer = stubPlayer()
            val players = listOf(
                stubPlayer(),
                excludedPlayer,
                stubPlayer()
            )
            private val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(players[0], players[2]).withPins(emptyList())
                )
            )
            val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersExceptExcluded() = setup(object : Setup() {
            val state = Start
        }) exercise {
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(tribe, rosteredPairAssignments, state)
            )
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players - excludedPlayer)
            }
        }
    }

    class GivenFourPlayersAndTwoPairs {
        open class Setup {
            val tribe = stubTribe()
            val players = listOf(
                stubPlayer(),
                stubPlayer(),
                stubPlayer(),
                stubPlayer()
            )
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = listOf(
                    pairOf(players[1], players[3]).withPins(emptyList()),
                    pairOf(players[0], players[2]).withPins(emptyList())
                )
            )

            val rosteredPairAssignments = rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(object : Setup() {
            val state = Start
        }) exercise {
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(
                    tribe,
                    rosteredPairAssignments,
                    state
                )
            )
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players)
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList()),
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList())
                    )
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
                    } else
                        next
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
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(
                    tribe,
                    rosteredPairAssignments,
                    state
                )
            )
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(firstAssignedPlayer)
                playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList()),
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList())
                    )
                )
            }
        }

        @Test
        fun whenShowingMidwayShownPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayShownPlayer = pairAssignments.pairs[1].players[0].player
            val state = ShowPlayer(midwayShownPlayer)
        }) exercise {
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(
                    tribe,
                    rosteredPairAssignments,
                    state
                )
            )
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(midwayShownPlayer)
                playersInRoster().assertIsEqualTo(
                    listOf(pairAssignments.pairs[1].players[1].player)
                )
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairAssignments.pairs[0],
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList())
                    )
                )
            }
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
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(
                    tribe,
                    rosteredPairAssignments,
                    state
                )
            )
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(placeholderPlayer)
                playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairOf(firstAssignedPlayer, placeholderPlayer).withPins(emptyList()),
                        pairOf(placeholderPlayer, placeholderPlayer).withPins(emptyList())
                    )
                )
            }
        }

        @Test
        fun whenShowingMidwayAssignedPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayAssignedPlayer = pairAssignments.pairs[1].players[0].player
            val state = AssignedPlayer(midwayAssignedPlayer)
        }) exercise {
            shallow(
                SpinAnimationPanel, SpinAnimationPanelProps(
                    tribe,
                    rosteredPairAssignments,
                    state
                )
            )
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(placeholderPlayer)
                playersInRoster().assertIsEqualTo(
                    listOf(pairAssignments.pairs[1].players[1].player)
                )
                shownPairAssignments().assertIsEqualTo(
                    listOf(
                        pairAssignments.pairs[0],
                        pairOf(midwayAssignedPlayer, placeholderPlayer).withPins(emptyList())
                    )
                )
            }
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
        private val styles = useStyles("pairassignments/SpinAnimation")

        private fun ShallowWrapper<dynamic>.playersInRoster() = findByClass(styles["playerRoster"])
            .findComponent(PlayerCard)
            .map { it.props().player }
            .toList()

        private fun ShallowWrapper<dynamic>.playerInSpotlight() = findByClass(styles["playerSpotlight"])
            .findComponent(PlayerCard).run {
                if (length == 1) props().player else null
            }

        private fun ShallowWrapper<dynamic>.shownPairAssignments() = findByClass(styles["pairAssignments"])
            .findComponent(AssignedPair)
            .map { it.props().pair }
            .toList()
    }

}