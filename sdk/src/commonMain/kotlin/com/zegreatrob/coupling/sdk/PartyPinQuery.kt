package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyPinsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PartyPinData = Triple<Party, List<Pin>, Pin>

data class PartyPinQuery(val partyId: PartyId, val pinId: String?) :
    SimpleSuspendAction<PartyPinQuery.Dispatcher, PartyPinData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyPinQuery): PartyPinData?
    }
}

interface ClientPartyPinQueryDispatcher : PartyIdGetSyntax, PartyPinsSyntax, PartyPinQuery.Dispatcher {
    override suspend fun perform(query: PartyPinQuery) = query.getData()

    private suspend fun PartyPinQuery.getData() = partyId.getData()
        ?.let { (party, pins) -> PartyPinData(party, pins, pins.findOrDefaultNew(pinId)) }

    private suspend fun PartyId.getData() = coroutineScope {
        await(async { get() }, async { getPins() })
    }.let { (party, pins) -> if (party == null) null else Pair(party, pins) }
}

private fun List<Pin>.findOrDefaultNew(pinId: String?) = find { it.id == pinId } ?: Pin()