package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.json.CreateSecretInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val createSecretResolver = dispatch(
    { request, _: JsonNull, args -> authorizedDispatcher(request, args.partyId) },
    { _, input: CreateSecretInput -> input.toCommand() },
    { true },
)

private fun CreateSecretInput.toCommand() = CreateSecretCommand(PartyId(partyId))
