package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val partyResolve = dispatch(
    { r, _, _ -> r.commandDispatcher },
    { entity: JsonParty, _: JsonElement -> PartyQuery(PartyId(entity.id)) },
    ::toJson,
)

private fun toJson(record: Record<PartyDetails>?) = record?.toSerializable()