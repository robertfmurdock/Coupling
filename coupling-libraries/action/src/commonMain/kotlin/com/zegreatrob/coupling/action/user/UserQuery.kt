package com.zegreatrob.coupling.action.user

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class UserQuery : SimpleSuspendAction<UserQueryDispatcher, User?> {
    override val performFunc = link(UserQueryDispatcher::perform)
}
