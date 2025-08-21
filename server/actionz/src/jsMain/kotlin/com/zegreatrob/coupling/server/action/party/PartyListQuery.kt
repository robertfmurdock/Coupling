package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@ActionMint
object PartyListQuery {

    interface Dispatcher :
        UserAuthenticatedPartyIdSyntax,
        UserPlayersSyntax,
        CurrentUserProvider,
        PartyRecordSyntax {

        suspend fun perform(query: PartyListQuery): PartyListResult = getPartiesAndUserPlayers()
            .onlyAuthenticatedParties()

        private suspend fun getPartiesAndUserPlayers() = getPartiesAndPlayersDeferred()
            .let { (partyDeferred, playerDeferred) -> partyDeferred.await() to playerDeferred.await() }

        private suspend fun getPartiesAndPlayersDeferred() = coroutineScope {
            async { getPartyRecords() } to async { currentUser.getPlayers() }
        }

        private suspend fun Pair<List<Record<PartyDetails>>, List<PartyRecord<Player>>>.onlyAuthenticatedParties() = let { (partyRecords, playerRecords) ->
            println("players $playerRecords")
            val ownedParties = partyRecords.filter(authorizedPartyIds().allowFilter())
            PartyListResult(
                ownedParties = ownedParties,
                playerParties = (partyRecords - ownedParties)
                    .filter(
                        playerRecords.map { it.data.partyId }.toSet().allowFilter(),
                    ),
            )
        }
    }
}

private fun Set<PartyId>.allowFilter(): (Record<PartyDetails>) -> Boolean = { contains(it.data.id) }

data class PartyListResult(val ownedParties: List<Record<PartyDetails>>, val playerParties: List<Record<PartyDetails>>)
