package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribePinQuery(val tribeId: TribeId, val pinId: String?) : Action

interface TribePinQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun TribePinQuery.perform() = logAsync {
        tribeId.getData()
            .let { (tribe, pins) ->
                Triple(
                    tribe,
                    pins,
                    pins.findOrDefaultNew(pinId)
                )
            }
    }

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = Pin()
