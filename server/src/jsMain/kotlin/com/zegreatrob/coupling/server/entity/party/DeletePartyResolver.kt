package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.DeletePartyInput
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePartyResolver: Resolver = dispatch<JsonNull, DeletePartyInput, CurrentPartyDispatcher, DeletePartyCommand, VoidResult, Boolean>(
    dispatcherFunc = requiredInput { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = requiredInput { _, input: DeletePartyInput -> DeletePartyCommand(input.partyId) },
    fireFunc = ::perform,
    toSerializable = { true },
)
