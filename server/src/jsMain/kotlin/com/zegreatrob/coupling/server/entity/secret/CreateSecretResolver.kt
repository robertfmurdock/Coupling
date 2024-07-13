package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.perform
import com.zegreatrob.coupling.json.CreateSecretInput
import com.zegreatrob.coupling.json.JsonSecretToken
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val createSecretResolver = dispatch<JsonNull, CreateSecretInput, CurrentPartyDispatcher, CreateSecretCommand, Pair<Secret, String>?, JsonSecretToken?>(
    dispatcherFunc = requiredInput { request, _: JsonNull, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = requiredInput { _, input: CreateSecretInput -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { it?.toModel() },
)

private fun CreateSecretInput.toCommand() = CreateSecretCommand(PartyId(partyId), description)
