package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.TribeIdPinsSyntax

data class PinsQuery(val tribeId: TribeId) : Action

interface PinsQueryDispatcher : ActionLoggingSyntax, TribeIdPinsSyntax {

    suspend fun PinsQuery.perform() = logAsync { tribeId.getPinsAsync().await() }

}
