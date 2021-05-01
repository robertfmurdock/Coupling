package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePinListQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribePinListQueryDispatcher, Pair<Tribe, List<Pin>>?> {
    override val performFunc = link(TribePinListQueryDispatcher::perform)
}

interface TribePinListQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinListQuery) = query.tribeId.getData()

    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { get() },
            async { getPins() }
        )
    }.let { (tribe, pins) -> if (tribe == null) null else tribe to pins }
}
