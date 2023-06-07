package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object UserQuery : SimpleSuspendAction<UserQuery.Dispatcher, User> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : AuthenticatedUserSyntax {
        suspend fun perform(query: UserQuery) = user
    }
}
