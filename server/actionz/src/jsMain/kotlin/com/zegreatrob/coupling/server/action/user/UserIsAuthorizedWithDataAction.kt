package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.server.action.party.CurrentConnectedUsersProvider
import com.zegreatrob.coupling.server.action.party.UserPlayersSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedWithDataAction(val partyId: PartyId) : SimpleSuspendAction<UserIsAuthorizedWithDataAction.Dispatcher, Result<Pair<PartyDetails, List<Player>>?>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        CurrentConnectedUsersProvider,
        UserPlayersSyntax,
        PartyIdGetSyntax,
        PartyPlayersSyntax {
        override val playerRepository: PlayerEmailRepository

        suspend fun perform(action: UserIsAuthorizedWithDataAction) = action.doWork().successResult()

        private suspend fun UserIsAuthorizedWithDataAction.doWork(): Pair<PartyDetails, List<Player>>? {
            val (connectedUsers, party, players) = coroutineScope {
                await(
                    async { loadCurrentConnectedUsers() },
                    async { partyId.get() },
                    async { partyId.getPlayerList() },
                )
            }
            val authorizedPartyIds = connectedUsers.flatMap { it.authorizedPartyIds }
            val playerEmails = players.flatMap { it.additionalEmails + listOf(it.email) }
            val authorized = authorizedPartyIds.contains(partyId) || connectedUsers.any { playerEmails.contains(it.email.toString()) }
            if (authorized) {
                if (party != null) {
                    return party to players
                }
            }
            return null
        }
    }
}
