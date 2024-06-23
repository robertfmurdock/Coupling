package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.DeleteSecretInput
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeleteSecretCommandDispatcher :
    DeleteSecretCommand.Dispatcher,
    GqlSyntax {
    override suspend fun perform(command: DeleteSecretCommand) =
        doQuery(Mutation.deleteSecret, command.toInput())
            .parseMutationResult()
            .toDomain()
            .deleteSecret
            ?.voidResult()
            ?: CommandResult.Unauthorized

    private fun DeleteSecretCommand.toInput() = DeleteSecretInput(partyId.value, secretId)
}
