package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DeletePairAssignmentsCommand(
    val partyId: PartyId,
    val pairAssignmentDocumentId: PairAssignmentDocumentId,
) : SimpleSuspendAction<DeletePairAssignmentsCommand.Dispatcher, VoidResult> {

    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePairAssignmentsCommand): VoidResult
    }
}
