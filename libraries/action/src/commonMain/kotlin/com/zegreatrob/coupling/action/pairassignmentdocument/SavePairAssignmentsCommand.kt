package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SavePairAssignmentsCommand(
    val partyId: PartyId,
    val pairAssignments: PairingSet,
) {
    fun interface Dispatcher {
        suspend fun perform(command: SavePairAssignmentsCommand): VoidResult
    }
}
