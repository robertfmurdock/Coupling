package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class RosteredPairAssignmentsTest {

    @Test
    fun whenPlayerIsNotInPairAssignmentsWillNotAppearInRoster() = setup(object {
        val excludedPlayer = stubPlayer()
        val players = listOf(
            stubPlayer(),
            excludedPlayer,
            stubPlayer(),
        )
        val pairAssignments = stubPairAssignmentDoc().copy(
            pairs = notEmptyListOf(
                pairOf(players[0], players[2]).withPins(emptySet()),
            ),
        )
    }) exercise {
        RosteredPairAssignments.rosteredPairAssignments(pairAssignments, players)
    } verify { rosteredPairAssignmentResult ->
        rosteredPairAssignmentResult.let {
            it.pairAssignments.assertIsEqualTo(pairAssignments)
            it.selectedPlayers.assertIsEqualTo(listOf(players[0], players[2]))
        }
    }
}
