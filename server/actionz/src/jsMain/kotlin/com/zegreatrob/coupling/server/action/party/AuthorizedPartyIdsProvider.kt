package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.UserConnectedUsersSyntax

interface AuthorizedPartyIdsProvider :
    CurrentUserProvider,
    UserConnectedUsersSyntax {
    suspend fun authorizedPartyIds() = (listOf(currentUser) + currentUser.connectedUsers())
        .flatMap { it.authorizedPartyIds }
        .toSet()
}
