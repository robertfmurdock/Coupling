package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.action.UserConnectedUsersSyntax

interface CurrentConnectedUsersProvider :
    CurrentUserProvider,
    UserConnectedUsersSyntax {
    suspend fun loadCurrentConnectedUsers(): List<UserDetails> = (listOf(currentUser) + currentUser.connectedUsers())
}
