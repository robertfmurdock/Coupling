package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.secret.perform
import com.zegreatrob.coupling.json.DeleteSecretInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deleteSecretResolver = dispatch<JsonNull, DeleteSecretInput, CurrentPartyDispatcher, DeleteSecretCommand, VoidResult, Boolean>(
    dispatcherFunc = requiredInput { request, _: JsonNull, args ->
        DispatcherProviders.authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _, args: DeleteSecretInput -> DeleteSecretCommand(PartyId(args.partyId), args.secretId) },
    fireFunc = ::perform,
    toSerializable = { true },
)
