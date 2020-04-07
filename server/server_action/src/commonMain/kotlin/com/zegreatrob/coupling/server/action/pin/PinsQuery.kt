package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinRecordsSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PinsQuery : Action

interface PinsQueryDispatcher : ActionLoggingSyntax, CurrentTribeIdSyntax, TribeIdPinRecordsSyntax {

    suspend fun PinsQuery.perform() = logAsync { currentTribeId.getPinRecords() }

}
