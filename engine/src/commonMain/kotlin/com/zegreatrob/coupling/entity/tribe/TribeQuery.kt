package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class TribeQuery(val tribeId: TribeId)

interface TribeQueryDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetSyntax, UserPlayersSyntax {

    suspend fun TribeQuery.perform() = getTribeAndPlayers().onlyAuthenticatedTribes()

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

