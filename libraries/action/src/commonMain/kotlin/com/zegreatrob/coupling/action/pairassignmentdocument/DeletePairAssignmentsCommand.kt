package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DeletePairAssignmentsCommand(
    val partyId: PartyId,
    val pairingSetId: PairingSetId,
) {
    fun interface Dispatcher {
        suspend fun perform(command: DeletePairAssignmentsCommand): VoidResult
    }
}
