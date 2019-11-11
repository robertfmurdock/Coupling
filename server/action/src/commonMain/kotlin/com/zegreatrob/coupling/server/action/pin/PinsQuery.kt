package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class PinsQuery(val tribeId: TribeId) : Action

interface PinsQueryDispatcher : ActionLoggingSyntax, TribeIdPinsSyntax {

    suspend fun PinsQuery.perform() = logAsync { tribeId.getPins() }

}
