package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.RosteredPairAssignments
import com.zegreatrob.coupling.model.get
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.external.Result
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.test.Test

@Suppress("unused")
class SpinAnimationTest {

    class GivenOnePlayerAndOnePair {
        open class Setup {
            val player = stubPlayer()
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = notEmptyListOf(pairOf(player).withPins(emptySet())),
            )
        }

        @Test
        fun startWillMoveToShowFirstAssignedPlayer() = setup(object : Setup() {
            val state = Start
        }) exercise {
            Start.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(ShowPlayer(player))
        }
    }

    class GivenThreePlayersAndOnePair {
        open class Setup {
            val party = stubPartyDetails()
            val excludedPlayer = stubPlayer()
            val players = listOf(
                stubPlayer(),
                excludedPlayer,
                stubPlayer(),
            )
            private val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = notEmptyListOf(pairOf(players[0], players[2]).withPins(emptySet())),
            )
            val rosteredPairAssignments = RosteredPairAssignments.rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersExceptExcluded() = setup(object : Setup() {
            val state = Start
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(
                    (players.map(Player::id) - excludedPlayer.id).map { it.value.toString() },
                )
            }
        }
    }

    class GivenFourPlayersAndTwoPairs {
        open class Setup {
            val party = stubPartyDetails()
            val players = listOf(
                stubPlayer(),
                stubPlayer(),
                stubPlayer(),
                stubPlayer(),
            )
            val pairAssignments = stubPairAssignmentDoc().copy(
                pairs = notEmptyListOf(
                    pairOf(players[1], players[3]).withPins(emptySet()),
                    pairOf(players[0], players[2]).withPins(emptySet()),
                ),
            )

            val rosteredPairAssignments = RosteredPairAssignments.rosteredPairAssignments(pairAssignments, players)
        }

        @Test
        fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(object : Setup() {
            val state = Start
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players.map { it.id.value.toString() })
                shownPairAssignments()
                    .assertIsEqualTo(listOf("?-??", "???-????"))
            }
        }

        @Test
        fun startWillMoveToShuffleStep1() = setup(object : Setup() {
            val state = Start
        }) exercise {
            Start.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(target = pairAssignments.pairs.head.players[0], step = 0))
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
            result.assertIsEqualTo(ShowPlayer(pairAssignments.pairs.head.players[0]))
        }

        @Test
        fun whenShowingFirstPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = players[1]
            val state = ShowPlayer(firstAssignedPlayer)
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(firstAssignedPlayer.id.value.toString())
            result.playersInRoster().assertIsEqualTo((players - firstAssignedPlayer).map { it.id.value.toString() })
            result.shownPairAssignments()
                .assertIsEqualTo(listOf("?-??", "???-????"))
        }

        @Test
        fun whenShowingMidwayShownPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayShownPlayer = pairAssignments.pairs[1].players[0]
            val state = ShowPlayer(midwayShownPlayer)
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify {
            it.playerInSpotlight().assertIsEqualTo(midwayShownPlayer.id.value.toString())
            it.playersInRoster().assertIsEqualTo(
                listOf(pairAssignments.pairs[1].players[1].id.value.toString()),
            )
            it.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairAssignments.pairs[0].players.toList().joinToString("-", transform = { it.id.value.toString() }),
                    "?-??",
                ),
            )
        }

        @Test
        fun showPlayerWillTransitionToAssigned() = setup(object : Setup() {
            val state = ShowPlayer(pairAssignments.pairs[0].players[0])
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(AssignedPlayer(pairAssignments.pairs[0].players[0]))
        }

        @Test
        fun whenShowingFirstAssignedPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = pairAssignments.pairs[0].players[0]
            val state = AssignedPlayer(firstAssignedPlayer)
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(placeholderPlayer.id.value.toString())
            result.playersInRoster().assertIsEqualTo((players - firstAssignedPlayer).map { it.id.value.toString() })
            result.shownPairAssignments()
                .assertIsEqualTo(listOf("${firstAssignedPlayer.id.value}-?", "??-???"))
        }

        @Test
        fun whenShowingMidwayAssignedPlayerWillContinueShowingPreviousAssignments() = setup(object : Setup() {
            val midwayAssignedPlayer = pairAssignments.pairs[1].players[0]
            val state = AssignedPlayer(midwayAssignedPlayer)
        }) exercise {
            render(SpinAnimationPanel.create(party, rosteredPairAssignments, state))
        } verify { result ->
            result.playerInSpotlight().assertIsEqualTo(placeholderPlayer.id.value.toString())
            result.playersInRoster().assertIsEqualTo(
                listOf(pairAssignments.pairs[1].players[1].id.value.toString()),
            )
            result.shownPairAssignments().assertIsEqualTo(
                listOf(
                    pairAssignments.pairs[0].players.toList().joinToString("-", transform = { it.id.value.toString() }),
                    listOf(midwayAssignedPlayer, placeholderPlayer).joinToString(
                        "-",
                        transform = { it.id.value.toString() },
                    ),
                ),
            )
        }

        @Test
        fun assignedPlayerWillTransitionToShuffleNextPlayerInPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[0])
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(pairAssignments.pairs[0].players[1], 0))
        }

        @Test
        fun assignedPlayerWillTransitionToShuffleNextPlayerInNextPair() = setup(object : Setup() {
            val state = AssignedPlayer(pairAssignments.pairs[0].players[1])
        }) exercise {
            state.next(pairAssignments)
        } verify { result ->
            result.assertIsEqualTo(Shuffle(pairAssignments.pairs[1].players[0], 0))
        }
    }

    companion object {

        private fun Result.playersInRoster() = baseElement.querySelector("[data-testid=player-roster]")
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }

        private fun Result.playerInSpotlight() = baseElement.getElementsByClassName("$playerSpotlightStyles")[0]
            ?.querySelectorAll("[data-player-id]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-player-id") }
            ?.firstOrNull()

        private fun Result.shownPairAssignments() = baseElement.querySelector("[data-testid=assigned-pairs]")
            ?.querySelectorAll("[data-assigned-pair]")
            ?.asList()
            ?.mapNotNull { it as? HTMLElement }
            ?.map { it.getAttribute("data-assigned-pair") }
    }
}
