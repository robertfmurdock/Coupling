package com.zegreatrob.coupling.action.user

import com.zegreatrob.coupling.model.user.User

interface UserQueryDispatcher {
    suspend fun perform(query: UserQuery): User?
}
