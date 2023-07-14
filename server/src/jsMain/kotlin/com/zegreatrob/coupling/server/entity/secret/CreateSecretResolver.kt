package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.fire
import com.zegreatrob.coupling.json.CreateSecretInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val createSecretResolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = { _, input: CreateSecretInput -> input.toCommand() },
    fireCommand = ::fire,
    toSerializable = { it?.toModel() },
)

private fun CreateSecretInput.toCommand() = CreateSecretCommand(PartyId(partyId))
