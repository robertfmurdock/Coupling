package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.spin.RosteredPairAssignments.Companion.rosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import stubPairAssignmentDoc
import stubPlayer
import kotlin.test.Test

class RosteredPairAssignmentsTest {

    @Test
    fun whenPlayerIsNotInPairAssignmentsWillNotAppearInRoster() = setup(object {
        val excludedPlayer = stubPlayer()
        val players = listOf(
            stubPlayer(),
            excludedPlayer,
            stubPlayer()
        )
        val pairAssignments = stubPairAssignmentDoc().copy(
            pairs = listOf(
                pairOf(players[0], players[2]).withPins(emptyList())
            )
        )
    }) exercise {
        rosteredPairAssignments(pairAssignments, players)
    } verify { result ->
        result.run {
            pairAssignments.assertIsEqualTo(pairAssignments)
            selectedPlayers.assertIsEqualTo(listOf(players[0], players[2]))
        }
    }

}