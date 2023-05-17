package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

typealias HistoryData = Pair<Party, List<PairAssignmentDocument>>

data class HistoryQuery(val partyId: PartyId) : SimpleSuspendAction<HistoryQuery.Dispatcher, HistoryData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: HistoryQuery): Pair<Party, List<PairAssignmentDocument>>?
    }
}

interface ClientHistoryQueryDispatcher : SdkProviderSyntax, HistoryQuery.Dispatcher {
    override suspend fun perform(query: HistoryQuery) = query.partyId.getData()

    private suspend fun PartyId.getData() = sdk.perform(
        graphQuery {
            party(this@getData) {
                party()
                pairAssignmentDocumentList()
            }
        },
    )?.partyData?.let {
        Pair(
            first = it.party?.data ?: return@let null,
            second = it.pairAssignmentDocumentList?.elements ?: return@let null,
        )
    }
}
