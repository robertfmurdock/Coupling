package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.DeleteTribeCommand
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders

val deleteTribeRoute = dispatch(command, ::deleteTribeCommand)
val deleteTribeResolver: Resolver = com.zegreatrob.coupling.server.graphql.dispatch(DispatcherProviders.command, { _, input ->
    DeleteTribeCommand(
    TribeId(input["tribeId"].toString())
) }, { true })

private fun deleteTribeCommand(request: Request) = with(request) { DeleteTribeCommand(tribeId()) }
