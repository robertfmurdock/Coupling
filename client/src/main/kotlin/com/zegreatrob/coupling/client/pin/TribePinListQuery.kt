package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePinListQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribePinListQuery, TribePinListQueryDispatcher, Pair<Tribe?, List<Pin>>> {
    override val perform = link(TribePinListQueryDispatcher::perform)
}

interface TribePinListQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinListQuery) = query.tribeId.getData()

    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { get() },
            async { getPins() }
        )
    }.successResult()
}
