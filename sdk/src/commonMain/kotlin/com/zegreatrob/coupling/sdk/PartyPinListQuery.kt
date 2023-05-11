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

data class PartyPinListQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyPinListQuery.Dispatcher, Pair<Party, List<Pin>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyPinListQuery): Pair<Party, List<Pin>>?
    }
}

interface ClientPartyPinListQueryDispatcher : PartyIdGetSyntax, PartyPinsSyntax, PartyPinListQuery.Dispatcher {
    override suspend fun perform(query: PartyPinListQuery) = query.partyId.getData()

    private suspend fun PartyId.getData() = coroutineScope {
        await(
            async { get() },
            async { getPins() },
        )
    }.let { (party, pins) -> if (party == null) null else party to pins }
}