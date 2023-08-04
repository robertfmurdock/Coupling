package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.boost.UserBoostQuery
import com.zegreatrob.coupling.model.user.CurrentUserProvider

interface ServerBoostQueryDispatcher : BoostGetSyntax, CurrentUserProvider, UserBoostQuery.Dispatcher {
    override suspend fun perform(query: UserBoostQuery) = load()
}
