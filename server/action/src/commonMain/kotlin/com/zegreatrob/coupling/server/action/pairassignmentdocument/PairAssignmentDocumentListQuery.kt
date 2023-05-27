package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

object PairAssignmentDocumentListQuery :
    SimpleSuspendResultAction<PairAssignmentDocumentListQuery.Dispatcher, List<PartyRecord<PairAssignmentDocument>>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax, CurrentPartyIdSyntax {
        suspend fun perform(query: PairAssignmentDocumentListQuery) = currentPartyId.loadPairAssignmentRecords()
            .successResult()
    }
}
