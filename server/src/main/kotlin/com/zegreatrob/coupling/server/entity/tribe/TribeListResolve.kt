package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.external.express.commandDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchCommand

val tribeListResolve = dispatchCommand(::commandDispatcher, { TribeListQuery }, ::toJson)

private fun toJson(records: List<Record<Tribe>>) = records.map { it.toJson().add(it.data.toJson()) }
    .toTypedArray()
