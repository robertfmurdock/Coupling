package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.entity.boost.adapt
import com.zegreatrob.coupling.server.graphql.dispatch

val partyDetailsResolve = dispatch(
    dispatcherFunc = adapt { r -> r.commandDispatcher },
    commandFunc = { entity: GqlParty, _ -> PartyQuery(entity.id) },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(record: Record<PartyDetails>?) = record?.toSerializable()
