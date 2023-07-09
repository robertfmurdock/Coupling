package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.json.CreateSecretInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val createSecretResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedPartyDispatcher(request, args.partyId) },
    queryFunc = { _, input: CreateSecretInput -> input.toCommand() },
    toSerializable = { it?.toModel() },
)

private fun CreateSecretInput.toCommand() = CreateSecretCommand(PartyId(partyId))
