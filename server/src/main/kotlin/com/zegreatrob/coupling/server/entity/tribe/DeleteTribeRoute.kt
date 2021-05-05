package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.DeleteTribeCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch

val deleteTribeResolver: Resolver = dispatch(command, { _, input ->
    DeleteTribeCommand(
        TribeId(input["tribeId"].toString())
    )
}, { true })
