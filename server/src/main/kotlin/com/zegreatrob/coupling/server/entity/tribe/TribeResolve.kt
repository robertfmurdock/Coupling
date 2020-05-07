package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.coupling.server.graphql.tribeId
import kotlin.js.Json

val tribeResolve = dispatch(command, ::toQuery, ::toJson)

private fun toQuery(entity: Json) = TribeQuery(TribeId(entity.tribeId()))

private fun toJson(record: Record<Tribe>?) = record?.run { toJson().add(data.toJson()) }