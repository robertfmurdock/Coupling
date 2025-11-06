package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.repository.party.PartyRecordSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@ActionMint
object PartyListQuery {

    interface Dispatcher :
        CurrentConnectedUsersProvider,
        UserPlayersSyntax,
        CurrentUserProvider,
        PartyRecordSyntax {

        suspend fun perform(query: PartyListQuery): PartyListResult = fetchAuthorizedPartyIds()
            .loadParties()

        private suspend fun fetchAuthorizedPartyIds() = coroutineScope {
            Pair(async { userAuthorizedPartyIds() }, async { playerAuthorizedPartyIds() })
        }.let { Pair(it.first.await(), it.second.await()) }

        private suspend fun Pair<Set<PartyId>, Set<PartyId>>.loadParties() = coroutineScope {
            val parties = getPartyRecords(first + second)
            PartyListResult(
                ownedParties = parties.filter { it.data.id in first },
                playerParties = parties.filter { it.data.id in second && it.data.id !in first },
            )
        }

        private suspend fun playerAuthorizedPartyIds(): Set<PartyId> = currentUser.loadPlayers()
            .map { it.data.partyId }
            .toSet()

        private suspend fun userAuthorizedPartyIds(): Set<PartyId> = loadCurrentConnectedUsers()
            .flatMap { it.authorizedPartyIds }
            .toSet()
    }
}

data class PartyListResult(val ownedParties: List<Record<PartyDetails>>, val playerParties: List<Record<PartyDetails>>)
