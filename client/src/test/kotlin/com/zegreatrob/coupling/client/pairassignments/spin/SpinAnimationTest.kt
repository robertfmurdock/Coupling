package com.zegreatrob.coupling.client.pairassignments.spin

import ShallowWrapper
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
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
        fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(Setup()) exercise {
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, Start))
        } verify { result ->
            result.apply {
                playersInRoster().assertIsEqualTo(players)
                shownPairAssignments().assertIsEqualTo(emptyList())
            }
        }

        @Test
        fun whenShowingFirstPlayerWillRemoveFromRosterAndShowInSpotlight() = setup(object : Setup() {
            val firstAssignedPlayer = players[1]
        }) exercise {
            shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, ShowPlayer(firstAssignedPlayer)))
        } verify { result ->
            result.apply {
                playerInSpotlight().assertIsEqualTo(firstAssignedPlayer)
                playersInRoster().assertIsEqualTo(players - firstAssignedPlayer)
                shownPairAssignments().assertIsEqualTo(emptyList())
            }
        }

    }

    companion object {
        private val styles = useStyles("pairassignments/SpinAnimation")

        private fun ShallowWrapper<dynamic>.playersInRoster() = findByClass(styles["playerRoster"])
            .findComponent(PlayerCard)
            .map { it.props().player }
            .toList()

        private fun ShallowWrapper<dynamic>.playerInSpotlight() = findByClass(styles["playerSpotlight"])
            .findComponent(PlayerCard).props().player

        private fun ShallowWrapper<dynamic>.shownPairAssignments() = findByClass(styles["pairAssignments"])
            .findComponent(AssignedPair)
            .map { it.props().pair }
            .toList()
    }

}