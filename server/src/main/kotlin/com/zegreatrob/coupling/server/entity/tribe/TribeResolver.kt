package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at

val tribeResolve = dispatch(command, { entity, _ -> TribeQuery(TribeId(entity.at("id")!!)) }, ::toJson)

private fun toJson(record: Record<Tribe>?) = record?.toSerializable()
