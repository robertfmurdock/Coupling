package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.await
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayersSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class UserIsAuthorizedWithDataAction(val tribeId: TribeId)

interface UserIsAuthorizedWithDataActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayersSyntax,
    TribeIdGetSyntax, TribeIdPlayersSyntax {

    suspend fun UserIsAuthorizedWithDataAction.perform(): Pair<KtTribe, List<Player>>? {
        val contains = getUserPlayers()
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
            async { tribeId.load() },
            async { tribeId.loadPlayers() }
        )
    }

}