package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe

object TribeListQuery

interface TribeListQueryDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayersSyntax, TribeListSyntax {

    suspend fun TribeListQuery.perform() = getTribesAndPlayers().onlyAuthenticatedTribes()

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
            .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private fun getTribesAndPlayersDeferred() =
            getTribesAsync() to getUserPlayersAsync()

    private fun Pair<List<KtTribe>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribes, players) ->
        tribes.filter(players.authenticatedFilter())
    }

}
