package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.repository.secret.SecretDelete

interface ServerDeleteSecretCommandDispatcher : DeleteSecretCommand.Dispatcher {

    val secretRepository: SecretDelete

    override suspend fun perform(command: DeleteSecretCommand) = secretRepository.deleteSecret(
        partyId = command.partyId,
        secretId = command.secret.id,
    ).voidResult()
}
