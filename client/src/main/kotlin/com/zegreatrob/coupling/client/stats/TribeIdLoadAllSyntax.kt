package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface TribeIdLoadAllSyntax : TribeIdGetSyntax, TribeIdPlayersSyntax, TribeIdHistorySyntax {
    suspend fun TribeId.loadAll() = coroutineScope {
        await(
            async { load()!! },
            async { loadPlayers() },
            async { loadHistory() }
        )
    }
}