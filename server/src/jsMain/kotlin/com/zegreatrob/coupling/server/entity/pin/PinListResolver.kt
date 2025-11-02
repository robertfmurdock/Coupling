package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.GqlPartyNode
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val pinListResolve = dispatch(
    dispatcherFunc = command(),
    commandFunc = { entity: GqlPartyNode, _: JsonElement? -> PinsQuery(entity.id) },
    fireFunc = ::perform,
    toSerializable = ::toSerializable,
)

private fun toSerializable(it: List<PartyRecord<Pin>>?) = it?.map(Record<PartyElement<Pin>>::toSerializable)
