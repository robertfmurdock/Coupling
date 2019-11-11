package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPlayersSyntax,
    TribeIdHistorySyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() = withContext(Dispatchers.Default) {
        await(
            async { load() },
            async { loadPlayers() },
            async { getHistory() }
        )
    }
}

suspend fun <T1, T2, T3> await(d1: Deferred<T1>, d2: Deferred<T2>, d3: Deferred<T3>) = Triple(
    d1.await(),
    d2.await(),
    d3.await()
)