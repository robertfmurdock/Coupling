package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
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
    SuspendAction<HistoryQueryDispatcher, Pair<Tribe?, List<PairAssignmentDocument>>> {
    override suspend fun execute(dispatcher: HistoryQueryDispatcher) = with(dispatcher) { perform() }
}

interface HistoryQueryDispatcher : TribeIdGetSyntax, TribeIdHistorySyntax {
    suspend fun HistoryQuery.perform() = tribeId.getData().successResult()

    private suspend fun TribeId.getData() = withContext(Dispatchers.Default) {
        await(
            async { get() },
            async { loadHistory() }
        )
    }
}
