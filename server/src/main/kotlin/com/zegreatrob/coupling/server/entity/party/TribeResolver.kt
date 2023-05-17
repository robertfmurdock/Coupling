package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.JsonElement

val partyResolve = dispatch(command, { entity, _: JsonElement -> PartyQuery(PartyId(entity.at("input")!!)) }, ::toJson)

private fun toJson(record: Record<Party>?) = record?.toSerializable()
