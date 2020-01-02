package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePinListQuery(val tribeId: TribeId) : Action

interface TribePinListQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun TribePinListQuery.perform() = logAsync { tribeId.getData() }
    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { get() },
            async { getPins() }
        )
    }
}
