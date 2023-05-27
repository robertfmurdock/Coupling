package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User

object UserQuery : SimpleSuspendResultAction<UserQuery.Dispatcher, User> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : AuthenticatedUserSyntax {
        suspend fun perform(query: UserQuery) = user.successResult()
    }
}
