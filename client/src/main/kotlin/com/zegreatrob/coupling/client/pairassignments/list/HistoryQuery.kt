package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

typealias HistoryData = Pair<Party, List<PairAssignmentDocument>>

data class HistoryQuery(val partyId: PartyId) : SimpleSuspendAction<HistoryQueryDispatcher, HistoryData?> {
    override val performFunc = link(HistoryQueryDispatcher::perform)
}

interface HistoryQueryDispatcher : PartyIdGetSyntax, PartyIdHistorySyntax {
    suspend fun perform(query: HistoryQuery) = query.partyId.getData()

    private suspend fun PartyId.getData() = withContext(Dispatchers.Default) {
        await(
            async { get() },
            async { loadHistory() },
        )
    }.let { (first, second) -> if (first == null) null else Pair(first, second) }
}
