package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId

data class DeletePairAssignmentsCommand(val partyId: PartyId, val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    SimpleSuspendResultAction<DeletePairAssignmentsCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePairAssignmentsCommand): com.zegreatrob.coupling.action.Result<Unit>
    }
}
