package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.secret.perform
import com.zegreatrob.coupling.json.GqlDeleteSecretInput
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deleteSecretResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        DispatcherProviders.authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _: JsonNull, args: GqlDeleteSecretInput ->
        args.toModel()
    },
    fireFunc = ::perform,
    toSerializable = { true },
)

fun GqlDeleteSecretInput.toModel(): DeleteSecretCommand? {
    return DeleteSecretCommand(
        partyId,
        SecretId(secretId) ?: return null,
    )
}
