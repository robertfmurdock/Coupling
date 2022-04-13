package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.DeleteTribeInput
import com.zegreatrob.coupling.server.action.connection.DeletePartyCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deleteTribeResolver: Resolver = dispatch(tribeCommand, { _, _: DeleteTribeInput -> DeletePartyCommand }, { true })
