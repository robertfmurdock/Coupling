package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
object UserQuery {
    interface Dispatcher : CurrentUserProvider {
        suspend fun perform(query: UserQuery) = currentUser
    }
}
