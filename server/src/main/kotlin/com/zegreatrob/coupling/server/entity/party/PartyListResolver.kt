package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyListResolve = dispatch(command(), { _: JsonNull, _: JsonNull -> PartyListQuery }, ::toJson)

private fun toJson(records: List<Record<PartyDetails>>?) = records?.map(Record<PartyDetails>::toSerializable)
