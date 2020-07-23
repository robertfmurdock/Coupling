package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
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
    SimpleSuspendResultAction<TribePinListQueryDispatcher, Pair<Tribe, List<Pin>>> {
    override val performFunc = link(TribePinListQueryDispatcher::perform)
}

interface TribePinListQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinListQuery): Result<Pair<Tribe, List<Pin>>> = query.tribeId.getData()
        ?.successResult()
        ?: NotFoundResult("Tribe")

    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { get() },
            async { getPins() }
        )
    }.let { (tribe, pins) -> if (tribe == null) null else tribe to pins }
}
