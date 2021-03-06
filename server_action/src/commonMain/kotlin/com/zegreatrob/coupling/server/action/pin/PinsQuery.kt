package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinRecordsSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

object PinsQuery :
    SimpleSuspendResultAction<PinsQueryDispatcher, List<TribeRecord<Pin>>> {
    override val performFunc = link(PinsQueryDispatcher::perform)
}

interface PinsQueryDispatcher : CurrentTribeIdSyntax, TribeIdPinRecordsSyntax {
    suspend fun perform(query: PinsQuery) = currentTribeId.getPinRecords().successResult()
}
