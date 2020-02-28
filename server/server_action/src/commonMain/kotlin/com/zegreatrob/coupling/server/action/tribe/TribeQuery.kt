package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.tribe.TribeIdGetRecordSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribeQuery(val tribeId: TribeId) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax,
    TribeIdGetRecordSyntax,
    UserPlayerIdsSyntax {

    suspend fun TribeQuery.perform() = logAsync { getTribeAndUserPlayerIds().onlyIfAuthenticated() }

    private suspend fun TribeQuery.getTribeAndUserPlayerIds() = coroutineScope {
        await(
            async { tribeId.loadRecord() },
            async { getUserPlayerIds() }
        )
    }

    private fun Pair<Record<Tribe>?, List<TribeElement<String>>>.onlyIfAuthenticated() = let { (tribe, players) ->
        tribe?.takeIf(players.authenticatedFilter())
    }

}

