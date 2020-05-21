package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class HistoryQuery(val tribeId: TribeId) :
    SimpleSuspendAction<HistoryQueryDispatcher, Pair<Tribe?, List<PairAssignmentDocument>>> {
    override val performFunc = link(HistoryQueryDispatcher::perform)
}

interface HistoryQueryDispatcher : TribeIdGetSyntax, TribeIdHistorySyntax {
    suspend fun perform(query: HistoryQuery) = query.tribeId.getData().successResult()

    private suspend fun TribeId.getData() = withContext(Dispatchers.Default) {
        await(
            async { get() },
            async { loadHistory() }
        )
    }
}
