package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val tribeListResolve = dispatch(command, { _, _: JsonElement -> TribeListQuery }, ::toJson)

private fun toJson(records: List<Record<Tribe>>) = records.map(Record<Tribe>::toSerializable)
