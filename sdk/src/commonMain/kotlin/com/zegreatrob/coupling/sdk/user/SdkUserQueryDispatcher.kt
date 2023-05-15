package com.zegreatrob.coupling.sdk.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.GraphQueries

interface SdkUserQueryDispatcher : UserQuery.Dispatcher, GqlSyntax, GraphQueries {
    override suspend fun perform(query: UserQuery): User? = queries.user.perform()?.user
}
