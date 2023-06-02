package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.DeletePartyInput
import com.zegreatrob.coupling.server.action.connection.DeletePartyCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePartyResolver: Resolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedDispatcher(request = request, partyId = args.partyId.value) },
    queryFunc = { _, _: DeletePartyInput -> DeletePartyCommand },
    toSerializable = { true },
)
