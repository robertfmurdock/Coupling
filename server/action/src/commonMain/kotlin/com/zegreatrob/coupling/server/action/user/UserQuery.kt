package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User

object UserQuery : SimpleSuspendResultAction<UserQueryDispatcher, User> {
    override val performFunc = link(UserQueryDispatcher::perform)
}

interface UserQueryDispatcher : AuthenticatedUserSyntax {

    suspend fun perform(query: UserQuery) = user.successResult()
}
