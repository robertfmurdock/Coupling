package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val partyResolve = dispatch(
    { r, entity, _ -> authorizedDispatcher(r, entity.id!!) },
    { entity: JsonPartyData, _: JsonElement -> PartyQuery(PartyId(entity.id!!)) },
    ::toJson,
)

private fun toJson(record: Record<Party>?) = record?.toSerializable()
