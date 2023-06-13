package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val pinListResolve = dispatch(
    command(),
    { entity: JsonPartyData, _: JsonElement -> PinsQuery(PartyId(entity.id)) },
    ::toSerializable,
)

private fun toSerializable(it: List<PartyRecord<Pin>>?) = it?.map(Record<PartyElement<Pin>>::toSerializable)
