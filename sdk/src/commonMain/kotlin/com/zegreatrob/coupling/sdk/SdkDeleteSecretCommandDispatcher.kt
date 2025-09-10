package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeleteSecretMutation
import com.zegreatrob.coupling.sdk.schema.type.DeleteSecretInput

interface SdkDeleteSecretCommandDispatcher :
    DeleteSecretCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeleteSecretCommand) = DeleteSecretMutation(command.toInput()).execute()
        .data?.deleteSecret?.voidResult()
        ?: CommandResult.Unauthorized

    private fun DeleteSecretCommand.toInput() = DeleteSecretInput(partyId = partyId, secretId = secretId)
}
