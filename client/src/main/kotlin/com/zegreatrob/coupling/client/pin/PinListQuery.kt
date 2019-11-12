package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class PinListQuery(val tribeId: TribeId) : Action

interface PinListQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun PinListQuery.perform() = logAsync { tribeId.getData() }
    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { load() },
            async { getPins() }
        )
    }
}