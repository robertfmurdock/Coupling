package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.party.UserPlayersSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
object UserPlayersQuery {
    interface Dispatcher :
        CurrentUserProvider,
        UserPlayersSyntax {
        suspend fun perform(query: UserPlayersQuery) = currentUser.getPlayers()
    }
}
