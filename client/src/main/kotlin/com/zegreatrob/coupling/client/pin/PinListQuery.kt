package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class PinListQuery(val tribeId: TribeId) : Action

interface PinListQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPinListSyntax {
    suspend fun PinListQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
            (getTribeAsync() to getPinListAsync())
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Pin>>>.await() = first.await() to second.await()

}