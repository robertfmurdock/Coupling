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

    private val styles = useStyles("pairassignments/SpinAnimation")

    @Test
    fun whenInStartStateWillShowAllPlayersAndNoPairs() = setup(object {
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
            stubPlayer(),
            stubPlayer()
        )
        val pairAssignments = stubPairAssignmentDoc().copy(
            pairs = listOf(
                pairOf(players[0], players[1]).withPins(emptyList()),
                pairOf(players[2], players[3]).withPins(emptyList())
            )
        )
    }) exercise {
        shallow(SpinAnimation, SpinAnimationProps(players, pairAssignments, Start))
    } verify { result ->
        result.apply {
            playersInRoster().assertIsEqualTo(players)
            shownPairAssignments().assertIsEqualTo(emptyList())
        }
    }

    private fun ShallowWrapper<dynamic>.playersInRoster() = findByClass(styles["playerRoster"])
        .findComponent(PlayerCard)
        .map { it.props().player }
        .toList()

    private fun ShallowWrapper<dynamic>.shownPairAssignments() = findByClass(styles["pairAssignments"])
        .findComponent(AssignedPair)
        .map { it.props().pair }
        .toList()
}