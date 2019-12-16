package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class HistoryQuery(val tribeId: TribeId) : Action

interface HistoryQueryDispatcher : ActionLoggingSyntax,
    TribeIdGetSyntax, TribeIdHistorySyntax {
    suspend fun HistoryQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() = withContext(Dispatchers.Default) {
        await(
            async { load() },
            async { loadHistory() }
        )
    }
}
