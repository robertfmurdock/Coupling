package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.JsonElement

val tribeResolve = dispatch(command, { entity, _: JsonElement -> TribeQuery(PartyId(entity.at("id")!!)) }, ::toJson)

private fun toJson(record: Record<Party>?) = record?.toSerializable()
