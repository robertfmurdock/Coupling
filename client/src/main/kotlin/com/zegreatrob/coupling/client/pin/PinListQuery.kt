package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

data class PinListQuery(val tribeId: TribeId) : Action

interface PinListQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun PinListQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
        (GlobalScope.async { load() } to getPinsAsync())
            .await()

    private suspend fun Pair<Deferred<KtTribe?>, Deferred<List<Pin>>>.await() = first.await() to second.await()

}