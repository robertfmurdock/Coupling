package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.GqlDeletePartyInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePartyResolver: Resolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _: JsonNull, input: GqlDeletePartyInput -> DeletePartyCommand(input.partyId) },
    fireFunc = ::perform,
    toSerializable = { true },
)
