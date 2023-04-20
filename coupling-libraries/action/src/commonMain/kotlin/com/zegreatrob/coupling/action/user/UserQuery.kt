package com.zegreatrob.coupling.action.user

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class UserQuery : SimpleSuspendAction<UserQuery.Dispatcher, User?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: UserQuery): User?
    }
}
