package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias TribePinData = Triple<Party, List<Pin>, Pin>

data class TribePinQuery(val tribeId: PartyId, val pinId: String?) :
    SimpleSuspendAction<TribePinQueryDispatcher, TribePinData?> {
    override val performFunc = link(TribePinQueryDispatcher::perform)
}

interface TribePinQueryDispatcher : TribeIdGetSyntax, TribeIdPinsSyntax {
    suspend fun perform(query: TribePinQuery) = query.getData()

    private suspend fun TribePinQuery.getData() = tribeId.getData()
        ?.let { (tribe, pins) -> TribePinData(tribe, pins, pins.findOrDefaultNew(pinId)) }

    private suspend fun PartyId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }.let { (tribe, pins) -> if (tribe == null) null else Pair(tribe, pins) }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = find { it.id == pinId } ?: Pin()
