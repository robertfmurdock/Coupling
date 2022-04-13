package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedPartyIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayerIdsSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedWithDataAction(val tribeId: PartyId) :
    SimpleSuspendResultAction<UserIsAuthorizedWithDataActionDispatcher, Pair<Party, List<Player>>?> {
    override val performFunc = link(UserIsAuthorizedWithDataActionDispatcher::perform)
}

interface UserIsAuthorizedWithDataActionDispatcher : UserAuthenticatedPartyIdSyntax, UserPlayerIdsSyntax,
    PartyIdGetSyntax, PartyPlayersSyntax {
    override val playerRepository: PlayerEmailRepository

    suspend fun perform(action: UserIsAuthorizedWithDataAction) = action.skdjflskdjf().successResult()

    private suspend fun UserIsAuthorizedWithDataAction.skdjflskdjf(): Pair<Party, List<Player>>? {
        val contains = getUserPlayerIds()
            .authenticatedTribeIds()
            .contains(tribeId)

        if (contains) {
            val (tribe, players) = loadTribeAndPlayers()

            if (tribe != null) {
                return tribe to players
            }
        }
        return null
    }

    private suspend fun UserIsAuthorizedWithDataAction.loadTribeAndPlayers() = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.getPlayerList() }
        )
    }

}