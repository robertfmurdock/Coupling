package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
object UserQuery {
    interface Dispatcher : AuthenticatedUserSyntax {
        suspend fun perform(query: UserQuery) = user
    }
}
