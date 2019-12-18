package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.server.action.AuthorizedTribeIdSyntax

object PinsQuery : Action

interface PinsQueryDispatcher : ActionLoggingSyntax, AuthorizedTribeIdSyntax, TribeIdPinsSyntax {

    suspend fun PinsQuery.perform() = logAsync { authorizedTribeId?.getPins() }

}
