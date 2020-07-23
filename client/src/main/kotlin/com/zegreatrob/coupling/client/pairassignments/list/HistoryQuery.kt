package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
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

typealias HistoryData = Pair<Tribe, List<PairAssignmentDocument>>

data class HistoryQuery(val tribeId: TribeId) :
    SimpleSuspendResultAction<HistoryQueryDispatcher, HistoryData> {
    override val performFunc = link(HistoryQueryDispatcher::perform)
}

interface HistoryQueryDispatcher : TribeIdGetSyntax, TribeIdHistorySyntax {
    suspend fun perform(query: HistoryQuery): Result<HistoryData> = query.tribeId.getData()?.successResult()
        ?: NotFoundResult("Tribe")

    private suspend fun TribeId.getData() = withContext(Dispatchers.Default) {
        await(
            async { get() },
            async { loadHistory() }
        )
    }.let { (first, second) -> if (first == null) null else Pair(first, second) }
}
