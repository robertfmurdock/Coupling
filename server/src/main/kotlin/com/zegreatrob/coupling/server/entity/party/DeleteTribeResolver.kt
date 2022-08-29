package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.DeletePartyInput
import com.zegreatrob.coupling.server.action.connection.DeletePartyCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deletePartyResolver: Resolver = dispatch(partyCommand, { _, _: DeletePartyInput -> DeletePartyCommand }, { true })
