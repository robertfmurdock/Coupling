package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class DeletePairAssignmentsCommand(
    val partyId: PartyId,
    val pairAssignmentDocumentId: PairAssignmentDocumentId,
) {

    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    fun interface Dispatcher {
        suspend fun perform(command: DeletePairAssignmentsCommand): VoidResult
    }
}
