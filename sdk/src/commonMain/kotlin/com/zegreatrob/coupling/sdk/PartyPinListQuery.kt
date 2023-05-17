package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyPinListQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyPinListQuery.Dispatcher, Pair<Party, List<Pin>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyPinListQuery): Pair<Party, List<Pin>>?
    }
}

interface ClientPartyPinListQueryDispatcher : SdkProviderSyntax, PartyPinListQuery.Dispatcher {
    override suspend fun perform(query: PartyPinListQuery) = query.partyId.getData()

    private suspend fun PartyId.getData() = sdk.perform(
        graphQuery {
            party(this@getData) {
                party()
                pinList()
            }
        },
    )?.partyData
        ?.let { it.party?.data to it.pinList?.elements }
        ?.let { (party, pins) -> if (party == null || pins == null) null else party to pins }
}
