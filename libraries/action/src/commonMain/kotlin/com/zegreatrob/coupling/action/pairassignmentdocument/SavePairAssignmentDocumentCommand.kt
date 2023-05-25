package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyElement

data class SavePairAssignmentDocumentCommand(val pairAssignmentDocument: PairAssignmentDocument) :
    SimpleSuspendResultAction<SavePairAssignmentDocumentCommand.Dispatcher, PartyElement<PairAssignmentDocument>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePairAssignmentDocumentCommand): Result<PartyElement<PairAssignmentDocument>>
    }
}
