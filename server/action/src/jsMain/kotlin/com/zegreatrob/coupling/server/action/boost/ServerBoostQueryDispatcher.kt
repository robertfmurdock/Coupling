package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.boost.PartyBoostQuery
import com.zegreatrob.coupling.action.boost.UserBoostQuery
import com.zegreatrob.coupling.model.user.CurrentUserProvider

interface ServerUserBoostQueryDispatcher :
    BoostGetSyntax,
    CurrentUserProvider,
    UserBoostQuery.Dispatcher {
    override suspend fun perform(query: UserBoostQuery) = load()
}

interface ServerPartyBoostQueryDispatcher :
    PartyIdBoostSyntax,
    CurrentUserProvider,
    PartyBoostQuery.Dispatcher {
    override suspend fun perform(query: PartyBoostQuery) = query.partyId.boost()
}
