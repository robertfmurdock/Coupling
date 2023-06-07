package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object PartyListQuery : SimpleSuspendAction<PartyListQuery.Dispatcher, List<Record<Party>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : UserAuthenticatedPartyIdSyntax, UserPlayerIdsSyntax, PartyRecordSyntax {

        suspend fun perform(query: PartyListQuery) = getPartiesAndUserPlayerIds()
            .onlyAuthenticatedParties()

        private suspend fun getPartiesAndUserPlayerIds() = getPartiesAndPlayersDeferred()
            .let { (partyDeferred, playerDeferred) -> partyDeferred.await() to playerDeferred.await() }

        private suspend fun getPartiesAndPlayersDeferred() = coroutineScope {
            async { getPartyRecords() } to async { getUserPlayerIds() }
        }

        private fun Pair<List<Record<Party>>, List<PartyElement<String>>>.onlyAuthenticatedParties() =
            let { (partyRecords, players) ->
                partyRecords.filter {
                    players.authenticatedFilter()(it)
                }
            }
    }
}
