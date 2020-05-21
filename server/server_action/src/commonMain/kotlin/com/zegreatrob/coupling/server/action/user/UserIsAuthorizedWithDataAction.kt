package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayerIdsSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedWithDataAction(val tribeId: TribeId) :
    SimpleSuspendAction<UserIsAuthorizedWithDataActionDispatcher, Pair<Tribe, List<Player>>?> {
    override val performFunc = link(UserIsAuthorizedWithDataActionDispatcher::perform)
}

interface UserIsAuthorizedWithDataActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax,
    TribeIdGetSyntax, TribeIdPlayersSyntax {
    override val playerRepository: PlayerEmailRepository

    suspend fun perform(action: UserIsAuthorizedWithDataAction) = action.skdjflskdjf().successResult()

    private suspend fun UserIsAuthorizedWithDataAction.skdjflskdjf(): Pair<Tribe, List<Player>>? {
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