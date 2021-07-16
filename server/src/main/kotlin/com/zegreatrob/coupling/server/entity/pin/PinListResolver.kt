package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val pinListResolve = dispatch(tribeCommand, { _, _: JsonElement -> PinsQuery }, ::toSerializable)

private fun toSerializable(it: List<TribeRecord<Pin>>) = it.map(Record<TribeIdPin>::toSerializable)
