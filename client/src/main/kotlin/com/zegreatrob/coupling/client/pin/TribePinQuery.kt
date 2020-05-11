package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SuspendAction
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
    SuspendAction<TribePinQueryDispatcher, Triple<Tribe?, List<Pin>, Pin>> {
    override suspend fun execute(dispatcher: TribePinQueryDispatcher) = with(dispatcher) { perform() }
}

interface TribePinQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun TribePinQuery.perform() = tribeId.getData()
        .let { (tribe, pins) ->
            Triple(
                tribe,
                pins,
                pins.findOrDefaultNew(pinId)
            )
        }.successResult()

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = find { it._id == pinId } ?: Pin()
