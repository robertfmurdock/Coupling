package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribeQuery(val tribeId: TribeId) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax,
    TribeIdGetSyntax,
    UserPlayerIdsSyntax {

    suspend fun TribeQuery.perform() = logAsync { getTribeAndUserPlayerIds().onlyIfAuthenticated() }

    private suspend fun TribeQuery.getTribeAndUserPlayerIds() = coroutineScope {
        await(
            async { tribeId.get() },
            async { getUserPlayerIds() }
        )
    }

    private fun Pair<Tribe?, List<TribeElement<String>>>.onlyIfAuthenticated() = let { (tribe, players) ->
        tribe?.takeIf(players.authenticatedFilter())
    }

}

