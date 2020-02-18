package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeRecordSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, UserPlayersSyntax,
    TribeRecordSyntax {

    suspend fun TribeListQuery.perform() = logAsync { getTribesAndPlayers().onlyAuthenticatedTribes() }

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
        .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private suspend fun getTribesAndPlayersDeferred() = coroutineScope {
        async { getTribeRecords() } to async { getUserPlayers() }
    }

    private fun Pair<List<Record<Tribe>>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() =
        let { (tribeRecords, players) ->
            tribeRecords.filter {
                players.authenticatedFilter()(it.data)
            }
        }

}
