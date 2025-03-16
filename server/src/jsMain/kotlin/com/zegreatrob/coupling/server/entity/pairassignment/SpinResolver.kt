package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.GqlSpinInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi

val spinResolver: Resolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _: JsonNull, args: GqlSpinInput -> args.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

@OptIn(ExperimentalKotoolsTypesApi::class)
private fun GqlSpinInput.command() = SpinCommand(
    partyId = partyId,
    playerIds = playerIds.toNotEmptyList().getOrThrow(),
    pinIds = pinIds,
)
