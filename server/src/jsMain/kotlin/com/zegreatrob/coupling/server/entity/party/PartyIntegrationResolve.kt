package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.server.action.party.PartyIntegrationQuery
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.entity.boost.adapt
import com.zegreatrob.coupling.server.graphql.dispatch

val partyIntegrationResolve = dispatch(
    dispatcherFunc = adapt { r -> r.commandDispatcher },
    commandFunc = { entity: JsonParty, _ -> entity.id?.let(::PartyId)?.let { PartyIntegrationQuery(it) } },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(record: Record<PartyIntegration>?) = record?.toSerializable()
