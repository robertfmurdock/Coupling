package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val spinResolver: Resolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = { _, args: SpinInput -> args.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SpinInput.command() = SpinCommand(
    partyId = partyId,
    playerIds = playerIds,
    pinIds = pinIds,
)
