package com.zegreatrob.coupling.client.pairassignments.spin

import ShallowWrapper
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findByClass
import findComponent
import shallow
import stubPairAssignmentDoc
import stubPlayer
import kotlin.test.Test

class SpinAnimationTest {

    class GivenFourPlayersAndTwoPairs {
        open class Setup {
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
        }

        @Test
        fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(object : Setup() {
            val state = Start
        }) exercise {
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, state))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players)
                shownPairAssignments().assertIsEqualTo(emptyList())
            }
        }

        @Test
        fun startWillMoveToShowFirstAssignedPlayer() = setup(object : Setup() {
            val state = Start
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(pairAssignments.pairs[0].players[0].player))
        }

        @Test
        fun whenShowingFirstPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = players[1]
            val state = ShowPlayer(firstAssignedPlayer)
        }) exercise {
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, state))
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(firstAssignedPlayer)
                playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(emptyList())
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
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, state))
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(Player("?", name = "Next..."))
                playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(
                    listOf(pairOf(firstAssignedPlayer).withPins(emptyList()))
                )
            }
        }

        @Test
        fun whenShowingMidwayAssignedPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayAssignedPlayer = pairAssignments.pairs[1].players[0].player
            val state = AssignedPlayer(midwayAssignedPlayer)
        }) exercise {
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, state))
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(Player("?", name = "Next..."))
                playersInRoster().assertIsEqualTo(players - midwayAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(
                    listOf(pairAssignments.pairs[0], pairOf(midwayAssignedPlayer).withPins(emptyList()))
                )
            }
        }

        @Test
        fun assignedPlayerWillTransitionToShowNextPlayerInPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[0].player)
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(pairAssignments.pairs[0].players[1].player))
        }

        @Test
        fun assignedPlayerWillTransitionToShowNextPlayerInNextPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[1].player)
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(pairAssignments.pairs[1].players[0].player))
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