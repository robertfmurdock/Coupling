package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.actionFunc.SimpleSuspendResultAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.repository.tribe.TribeRecordSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object TribeListQuery :
    SimpleSuspendResultAction<TribeListQueryDispatcher, List<Record<Tribe>>> {
    override val performFunc = link(TribeListQueryDispatcher::perform)
}

interface TribeListQueryDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax, TribeRecordSyntax {

    suspend fun perform(query: TribeListQuery) = getTribesAndUserPlayerIds()
        .onlyAuthenticatedTribes()
        .successResult()

    private suspend fun getTribesAndUserPlayerIds() = getTribesAndPlayersDeferred()
        .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private suspend fun getTribesAndPlayersDeferred() = coroutineScope {
        async { getTribeRecords() } to async { getUserPlayerIds() }
    }

    private fun Pair<List<Record<Tribe>>, List<TribeElement<String>>>.onlyAuthenticatedTribes() =
        let { (tribeRecords, players) ->
            tribeRecords.filter {
                players.authenticatedFilter()(it)
            }
        }

}
