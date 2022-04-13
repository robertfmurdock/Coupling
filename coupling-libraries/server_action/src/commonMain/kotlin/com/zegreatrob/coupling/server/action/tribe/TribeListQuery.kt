package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.tribe.TribeRecordSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object TribeListQuery : SimpleSuspendResultAction<PartyListQueryDispatcher, List<Record<Party>>> {
    override val performFunc = link(PartyListQueryDispatcher::perform)
}

interface PartyListQueryDispatcher : UserAuthenticatedPartyIdSyntax, UserPlayerIdsSyntax, TribeRecordSyntax {

    suspend fun perform(query: TribeListQuery) = getTribesAndUserPlayerIds()
        .onlyAuthenticatedTribes()
        .successResult()

    private suspend fun getTribesAndUserPlayerIds() = getTribesAndPlayersDeferred()
        .let { (tribeDeferred, playerDeferred) -> tribeDeferred.await() to playerDeferred.await() }

    private suspend fun getTribesAndPlayersDeferred() = coroutineScope {
        async { getTribeRecords() } to async { getUserPlayerIds() }
    }

    private fun Pair<List<Record<Party>>, List<PartyElement<String>>>.onlyAuthenticatedTribes() =
        let { (tribeRecords, players) ->
            tribeRecords.filter {
                players.authenticatedFilter()(it)
            }
        }

}
