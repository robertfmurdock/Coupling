package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.orderedPairedPlayers
import com.zegreatrob.coupling.model.player.Player

class RosteredPairAssignments private constructor(
    val pairAssignments: PairingSet,
    val selectedPlayers: List<Player>,
) {
    companion object {
        fun rosteredPairAssignments(pairAssignments: PairingSet, allPlayers: List<Player>) = RosteredPairAssignments(
            pairAssignments,
            pairAssignments.orderedPairedPlayers().let { allPlayers.filter(it::contains) },
        )
    }
}
