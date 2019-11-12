package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPlayersSyntax,
    TribeIdHistorySyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() = coroutineScope {
        await(
            async { load() },
            async { loadPlayers() },
            async { getHistory() }
        )
    }
}
