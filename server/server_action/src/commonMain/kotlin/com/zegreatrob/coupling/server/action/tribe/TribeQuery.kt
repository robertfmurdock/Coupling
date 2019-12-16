package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TribeQuery(val tribeId: TribeId) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax,
    TribeIdGetSyntax,
    UserPlayersSyntax {

    suspend fun TribeQuery.perform() = logAsync { getTribeAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun TribeQuery.getTribeAndPlayers() = coroutineScope {
        await(
            async { tribeId.load() },
            async { getUserPlayers() }
        )
    }

    private fun Pair<Tribe?, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribe, players) ->
        tribe?.takeIf(players.authenticatedFilter())
    }

}

