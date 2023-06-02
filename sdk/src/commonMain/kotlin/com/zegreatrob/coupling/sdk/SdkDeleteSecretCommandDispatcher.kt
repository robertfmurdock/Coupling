package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.DeleteSecretCommand
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.json.DeleteSecretInput
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeleteSecretCommandDispatcher : DeleteSecretCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: DeleteSecretCommand): Result<Boolean> =
        doQuery(Mutation.deleteSecret, command.toInput())
            .parseMutationResult()
            .toDomain()
            .deleteSecret
            ?.successResult()
            ?: NotFoundResult("secret")

    private fun DeleteSecretCommand.toInput() = DeleteSecretInput(partyId.value, secret.id)
}
