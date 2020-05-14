package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
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
    override val perform = link(UserIsAuthorizedWithDataActionDispatcher::perform)
}

interface UserIsAuthorizedWithDataActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax,
    TribeIdGetSyntax, TribeIdPlayersSyntax, ActionLoggingSyntax {
    override val playerRepository: PlayerEmailRepository

    suspend fun perform(action: UserIsAuthorizedWithDataAction) = action.skdjflskdjf()

    private suspend fun UserIsAuthorizedWithDataAction.skdjflskdjf() = logAsync {
        val contains = getUserPlayerIds()
            .authenticatedTribeIds()
            .contains(tribeId)

        if (contains) {
            val (tribe, players) = loadTribeAndPlayers()

            if (tribe != null) {
                return@logAsync tribe to players
            }
        }
        null
    }.successResult()

    private suspend fun UserIsAuthorizedWithDataAction.loadTribeAndPlayers() = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.getPlayerList() }
        )
    }

}