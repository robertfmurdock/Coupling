package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax
import com.zegreatrob.coupling.server.action.SuspendAction

object PinsQuery : SuspendAction<PinsQueryDispatcher, List<TribeRecord<Pin>>> {
    override suspend fun execute(dispatcher: PinsQueryDispatcher) = with(dispatcher) { perform() }
}

interface PinsQueryDispatcher : CurrentTribeIdSyntax, TribeIdPinRecordsSyntax {
    suspend fun PinsQuery.perform() = currentTribeId.getPinRecords()
}
