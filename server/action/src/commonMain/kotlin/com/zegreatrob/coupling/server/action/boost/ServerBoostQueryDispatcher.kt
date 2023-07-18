package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.model.user.CurrentUserProvider

interface ServerBoostQueryDispatcher : BoostGetSyntax, CurrentUserProvider, BoostQuery.Dispatcher {
    override suspend fun perform(command: BoostQuery) = load()
}
