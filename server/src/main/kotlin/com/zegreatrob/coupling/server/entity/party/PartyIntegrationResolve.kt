package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.server.action.party.PartyIntegrationQuery
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val partyIntegrationResolve = dispatch(
    { r, _, _ -> r.commandDispatcher },
    { entity: JsonParty, _: JsonElement -> PartyIntegrationQuery(PartyId(entity.id)) },
    ::toJson,
)

private fun toJson(record: Record<PartyIntegration>?) = record?.toSerializable()
