package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val partyDetailsResolve = dispatch(
    dispatcherFunc = { r, _, _ -> r.commandDispatcher },
    commandFunc = { entity: JsonParty, _: JsonElement? -> entity.id?.let(::PartyId)?.let { PartyQuery(it) } },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(record: Record<PartyDetails>?) = record?.toSerializable()
