package com.zegreatrob.coupling.sdk

import com.example.DeleteSecretMutation
import com.example.type.DeleteSecretInput
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkDeleteSecretCommandDispatcher :
    DeleteSecretCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeleteSecretCommand) = apolloMutation(DeleteSecretMutation(command.toInput()))
        .data?.deleteSecret?.voidResult()
        ?: CommandResult.Unauthorized

    private fun DeleteSecretCommand.toInput() = DeleteSecretInput(partyId = partyId, secretId = secretId)
}
