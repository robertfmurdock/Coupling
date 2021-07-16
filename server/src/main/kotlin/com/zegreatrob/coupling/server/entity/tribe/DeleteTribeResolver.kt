package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.connection.DeleteTribeCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val deleteTribeResolver: Resolver = dispatch(tribeCommand, { _, _: JsonElement -> DeleteTribeCommand }, { true })
