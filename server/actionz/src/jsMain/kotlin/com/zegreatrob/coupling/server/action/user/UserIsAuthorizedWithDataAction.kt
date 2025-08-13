package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.server.action.party.UserAuthenticatedPartyIdSyntax
import com.zegreatrob.coupling.server.action.party.UserPlayerIdsSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedWithDataAction(val partyId: PartyId) : SimpleSuspendResultAction<UserIsAuthorizedWithDataAction.Dispatcher, Pair<PartyDetails, List<Player>>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        UserAuthenticatedPartyIdSyntax,
        UserPlayerIdsSyntax,
        PartyIdGetSyntax,
        PartyPlayersSyntax {
        override val playerRepository: PlayerEmailRepository

        suspend fun perform(action: UserIsAuthorizedWithDataAction) = action.doWork().successResult()

        private suspend fun UserIsAuthorizedWithDataAction.doWork(): Pair<PartyDetails, List<Player>>? {
            val contains = getUserPlayers(currentUser.email)
                .authenticatedPartyIds()
                .contains(partyId)

            if (contains) {
                val (party, players) = loadPartyAndPlayers()

                if (party != null) {
                    return party to players
                }
            }
            return null
        }

        private suspend fun UserIsAuthorizedWithDataAction.loadPartyAndPlayers() = coroutineScope {
            await(
                async { partyId.get() },
                async { partyId.getPlayerList() },
            )
        }
    }
}
