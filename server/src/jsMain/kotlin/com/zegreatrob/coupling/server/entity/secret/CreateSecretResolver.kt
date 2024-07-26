package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.perform
import com.zegreatrob.coupling.json.GqlCreateSecretInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val createSecretResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = requiredInput { _: JsonNull, input: GqlCreateSecretInput -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { it?.toModel() },
)

private fun GqlCreateSecretInput.toCommand() =
    CreateSecretCommand(PartyId(partyId), description)
