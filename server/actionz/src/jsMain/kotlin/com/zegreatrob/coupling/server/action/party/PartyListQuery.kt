package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@ActionMint
object PartyListQuery {

    interface Dispatcher :
        UserAuthenticatedPartyIdSyntax,
        UserPlayerIdsSyntax,
        CurrentUserProvider,
        PartyRecordSyntax {

        suspend fun perform(query: PartyListQuery) = getPartiesAndUserPlayerIds()
            .onlyAuthenticatedParties()

        private suspend fun getPartiesAndUserPlayerIds() = getPartiesAndPlayersDeferred()
            .let { (partyDeferred, playerDeferred) -> partyDeferred.await() to playerDeferred.await() }

        private suspend fun getPartiesAndPlayersDeferred() = coroutineScope {
            async { getPartyRecords() } to async { getUserPlayerIds(currentUser.email) }
        }

        private fun Pair<List<Record<PartyDetails>>, List<PartyElement<PlayerId>>>.onlyAuthenticatedParties() = let { (partyRecords, players) ->
            partyRecords.filter {
                players.authenticatedFilter()(it)
            }
        }
    }
}
