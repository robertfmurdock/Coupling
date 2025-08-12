package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
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

        suspend fun perform(query: PartyListQuery): PartyListResult = getPartiesAndUserPlayerIds()
            .onlyAuthenticatedParties()

        private suspend fun getPartiesAndUserPlayerIds() = getPartiesAndPlayersDeferred()
            .let { (partyDeferred, playerDeferred) -> partyDeferred.await() to playerDeferred.await() }

        private suspend fun getPartiesAndPlayersDeferred() = coroutineScope {
            async { getPartyRecords() } to async { getUserPlayerIds(currentUser.email) }
        }

        private fun Pair<List<Record<PartyDetails>>, List<PartyElement<PlayerId>>>.onlyAuthenticatedParties() = let { (partyRecords, playerIds) ->
            val ownedParties = partyRecords.filter(authorizedPartyIds().allowFilter())
            PartyListResult(
                ownedParties = ownedParties,
                playerParties = (partyRecords - ownedParties)
                    .filter(
                        playerIds.map { it.partyId }.toSet().allowFilter(),
                    ),
            )
        }
    }
}

private fun Set<PartyId>.allowFilter(): (Record<PartyDetails>) -> Boolean = { contains(it.data.id) }

data class PartyListResult(val ownedParties: List<Record<PartyDetails>>, val playerParties: List<Record<PartyDetails>>)
