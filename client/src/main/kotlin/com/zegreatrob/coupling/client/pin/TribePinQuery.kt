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

data class TribePinQuery(val tribeId: TribeId, val pinId: String?) :
    SimpleSuspendAction<TribePinQueryDispatcher, Triple<Tribe?, List<Pin>, Pin>> {
    override val performFunc = link(TribePinQueryDispatcher::perform)
}

interface TribePinQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinQuery) = query.getData().successResult()

    private suspend fun TribePinQuery.getData() = tribeId.getData()
        .let { (tribe, pins) -> plusPinWithId(tribe, pins, pinId) }

    private fun plusPinWithId(tribe: Tribe?, pins: List<Pin>, pinId: String?) =
        Triple(tribe, pins, pins.findOrDefaultNew(pinId))

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = find { it._id == pinId } ?: Pin()
