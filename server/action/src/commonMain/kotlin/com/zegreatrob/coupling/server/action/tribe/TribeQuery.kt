package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax

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

