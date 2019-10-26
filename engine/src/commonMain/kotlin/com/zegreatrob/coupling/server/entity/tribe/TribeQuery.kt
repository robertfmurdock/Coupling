package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.core.entity.tribe.KtTribe
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class TribeQuery(val tribeId: TribeId) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, TribeIdGetSyntax, UserPlayersSyntax {

    suspend fun TribeQuery.perform() = logAsync { getTribeAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun TribeQuery.getTribeAndPlayers() = getTribeAndPlayersDeferred()
            .let { (tribeDeferred, playerDeferred) ->
                Pair(tribeDeferred.await(), playerDeferred.await())
            }

    private fun TribeQuery.getTribeAndPlayersDeferred() =
            tribeId.loadAsync() to getUserPlayersAsync()

    private fun Pair<KtTribe?, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribe, players) ->
        tribe?.takeIf(players.authenticatedFilter())
    }

}

