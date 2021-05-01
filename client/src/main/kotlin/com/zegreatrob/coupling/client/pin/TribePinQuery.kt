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

typealias TribePinData = Triple<Tribe, List<Pin>, Pin>

data class TribePinQuery(val tribeId: TribeId, val pinId: String?) :
    SimpleSuspendAction<TribePinQueryDispatcher, TribePinData?> {
    override val performFunc = link(TribePinQueryDispatcher::perform)
}

interface TribePinQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinQuery) = query.getData()

    private suspend fun TribePinQuery.getData() = tribeId.getData()
        ?.let { (tribe, pins) -> TribePinData(tribe, pins, pins.findOrDefaultNew(pinId)) }

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }.let { (tribe, pins) -> if (tribe == null) null else Pair(tribe, pins) }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = find { it.id == pinId } ?: Pin()
