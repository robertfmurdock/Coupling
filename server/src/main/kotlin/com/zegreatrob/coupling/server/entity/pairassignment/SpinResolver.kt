package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.json.JsonPairAssignmentDocument
import com.zegreatrob.coupling.json.SpinInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull

val spinResolver: Resolver = dispatch(
    { request, _: JsonNull, args ->
        authorizedDispatcher(
            request = request,
            partyId = args.partyId.value,
        )
    },
    { _, args: SpinInput -> args.command() },
    { true },
)

private fun SpinInput.command() = SpinCommand(
    partyId = partyId,
    playerIds = playerIds,
    pinIds = pinIds,
)

@Serializable
data class Return(val result: JsonPairAssignmentDocument)
