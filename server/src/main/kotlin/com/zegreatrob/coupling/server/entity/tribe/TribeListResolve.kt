package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch

val tribeListResolve = dispatch(command, { TribeListQuery }, ::toJson)

private fun toJson(records: List<Record<Tribe>>) = records.map { it.toJson().add(it.data.toJson()) }
    .toTypedArray()
