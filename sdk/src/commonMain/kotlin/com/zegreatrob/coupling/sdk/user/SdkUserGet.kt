package com.zegreatrob.coupling.sdk.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.action.user.UserQueryDispatcher
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.user.UserGet

interface SdkUserGet : UserQueryDispatcher, UserGet {
    override suspend fun getUser() = perform(UserQuery())?.let { Record(it, "") }
}