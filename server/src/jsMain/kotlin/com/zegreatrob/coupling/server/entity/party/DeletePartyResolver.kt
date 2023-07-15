package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.DeletePartyInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val deletePartyResolver: Resolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = { _, input: DeletePartyInput -> DeletePartyCommand(input.partyId) },
    fireFunc = ::perform,
    toSerializable = { true },
)
