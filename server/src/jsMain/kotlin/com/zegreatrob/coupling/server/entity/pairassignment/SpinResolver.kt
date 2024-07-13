package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val spinResolver: Resolver = dispatch<JsonNull, SpinInput, CurrentPartyDispatcher, SpinCommand, SpinCommand.Result, Boolean>(
    dispatcherFunc = requiredInput { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = requiredInput { _, args: SpinInput -> args.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SpinInput.command() = SpinCommand(
    partyId = partyId,
    playerIds = playerIds,
    pinIds = pinIds,
)
