package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.entity.boost.adapt
import com.zegreatrob.coupling.server.graphql.GqlPartyNode
import com.zegreatrob.coupling.server.graphql.dispatch

inline fun <reified T> getPartyDetailsResolve(crossinline toJson: (Record<PartyDetails>?) -> T) = dispatch(
    dispatcherFunc = adapt { r -> r.commandDispatcher },
    commandFunc = { entity: GqlPartyNode, _ -> PartyQuery(entity.id) },
    fireFunc = ::perform,
    toSerializable = toJson,
)
