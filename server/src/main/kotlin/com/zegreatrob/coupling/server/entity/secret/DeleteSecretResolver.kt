package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.json.DeleteSecretInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deleteSecretResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args ->
        DispatcherProviders.authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    queryFunc = { _, args: DeleteSecretInput -> DeleteSecretCommand(PartyId(args.partyId), Secret(args.secretId)) },
    toSerializable = { true },
)
