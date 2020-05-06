package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.graphql.dispatchCommand
import com.zegreatrob.coupling.server.graphql.commandDispatcher
import com.zegreatrob.coupling.server.graphql.tribeId
import kotlin.js.Json

val tribeResolve = dispatchCommand(::commandDispatcher, ::toQuery, ::toJson)

private fun toQuery(entity: Json) = TribeQuery(TribeId(entity.tribeId()))

private fun toJson(record: Record<Tribe>?) = record?.run { toJson().add(data.toJson()) }