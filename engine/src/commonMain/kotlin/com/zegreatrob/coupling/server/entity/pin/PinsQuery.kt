package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.TribeIdPinsSyntax

data class PinsQuery(val tribeId: TribeId) : Action

interface PinsQueryDispatcher : ActionLoggingSyntax, TribeIdPinsSyntax {

    suspend fun PinsQuery.perform() = logAsync { tribeId.getPinsAsync().await() }

}
