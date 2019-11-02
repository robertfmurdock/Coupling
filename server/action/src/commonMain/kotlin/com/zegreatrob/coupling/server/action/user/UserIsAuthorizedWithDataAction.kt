package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayersSyntax

data class UserIsAuthorizedWithDataAction(val tribeId: TribeId)

interface UserIsAuthorizedWithDataActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayersSyntax,
    TribeIdGetSyntax, TribeIdPlayersSyntax {

    suspend fun UserIsAuthorizedWithDataAction.perform(): Pair<KtTribe, List<Player>>? {
        val contains = getUserPlayersAsync().await()
            .authenticatedTribeIds()
            .contains(tribeId)

        if (contains) {
            val tribeAsync = tribeId.loadAsync()
            val playersAsync = tribeId.loadPlayers()
            val tribe = tribeAsync.await()

            if (tribe != null) {
                return tribe to playersAsync
            }
        }

        return null
    }

}