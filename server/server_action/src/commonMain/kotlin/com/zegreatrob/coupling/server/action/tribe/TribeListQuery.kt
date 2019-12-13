package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeListSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, UserPlayersSyntax,
    TribeListSyntax {

    suspend fun TribeListQuery.perform() = logAsync { getTribesAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
        .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private suspend fun getTribesAndPlayersDeferred() = coroutineScope {
        async { getTribes() } to async { getUserPlayers() }
    }

    private fun Pair<List<Tribe>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribes, players) ->
        tribes.filter(players.authenticatedFilter())
    }

}
