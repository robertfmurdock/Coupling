package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

typealias HistoryData = Pair<Party, List<PairAssignmentDocument>>

data class HistoryQuery(val partyId: PartyId) : SimpleSuspendAction<HistoryQuery.Dispatcher, HistoryData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: HistoryQuery): Pair<Party, List<PairAssignmentDocument>>?
    }
}

interface ClientHistoryQueryDispatcher : SdkProviderSyntax, HistoryQuery.Dispatcher {
    override suspend fun perform(query: HistoryQuery) = query.partyId.getData()

    private suspend fun PartyId.getData() = withContext(Dispatchers.Default) {
        await(
            async {
                sdk.perform(graphQuery { party(this@getData) { party() } })
                    ?.partyData
                    ?.party?.data
            },
            async { sdk.getPairAssignments(this@getData).elements },
        )
    }.let { (first, second) -> if (first == null) null else Pair(first, second) }
}
