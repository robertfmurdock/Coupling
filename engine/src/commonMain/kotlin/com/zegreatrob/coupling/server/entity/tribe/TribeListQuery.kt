package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.core.entity.tribe.KtTribe

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, UserPlayersSyntax, TribeListSyntax {

    suspend fun TribeListQuery.perform() = logAsync { getTribesAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
            .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private fun getTribesAndPlayersDeferred() =
            getTribesAsync() to getUserPlayersAsync()

    private fun Pair<List<KtTribe>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribes, players) ->
        tribes.filter(players.authenticatedFilter())
    }

}
