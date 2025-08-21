package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.action.party.CurrentConnectedUsersProvider
import com.zegreatrob.coupling.server.action.party.UserPlayersSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedAction(val partyId: PartyId) : SimpleSuspendResultAction<UserIsAuthorizedAction.Dispatcher, Boolean> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        CurrentConnectedUsersProvider,
        UserPlayersSyntax {

        suspend fun perform(action: UserIsAuthorizedAction) = authorizedPartyIds()
            .contains(action.partyId)
            .successResult()

        private suspend fun authorizedPartyIds(): List<PartyId> = coroutineScope {
            listOf(async { playerAuthorizedPartyIds() }, async { userAuthorizedPartyIds() })
                .awaitAll()
                .flatten()
        }

        private suspend fun userAuthorizedPartyIds(): List<PartyId> = loadCurrentConnectedUsers().flatMap(UserDetails::authorizedPartyIds)

        private suspend fun playerAuthorizedPartyIds(): List<PartyId> = currentUser.loadPlayers()
            .map { it.data.partyId }
    }
}
