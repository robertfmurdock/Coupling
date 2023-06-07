package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object PairAssignmentDocumentListQuery :
    SimpleSuspendAction<PairAssignmentDocumentListQuery.Dispatcher, List<PartyRecord<PairAssignmentDocument>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyIdPairAssignmentRecordsSyntax, CurrentPartyIdSyntax {
        suspend fun perform(query: PairAssignmentDocumentListQuery) = currentPartyId.loadPairAssignmentRecords()
    }
}
