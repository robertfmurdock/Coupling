package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeListSyntax
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, UserPlayersSyntax,
    TribeListSyntax {

    suspend fun TribeListQuery.perform() = logAsync { getTribesAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
        .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private fun getTribesAndPlayersDeferred() =
        GlobalScope.async { getTribes() } to getUserPlayersAsync()

    private fun Pair<List<KtTribe>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribes, players) ->
        tribes.filter(players.authenticatedFilter())
    }

}
