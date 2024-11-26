package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.orderedPairedPlayers
import com.zegreatrob.coupling.model.player.Player

class RosteredPairAssignments private constructor(
    val pairAssignments: PairAssignmentDocument,
    val selectedPlayers: List<Player>,
) {
    companion object {
        fun rosteredPairAssignments(pairAssignments: PairAssignmentDocument, allPlayers: List<Player>) = RosteredPairAssignments(
            pairAssignments,
            pairAssignments.orderedPairedPlayers().let { allPlayers.filter(it::contains) },
        )
    }
}
