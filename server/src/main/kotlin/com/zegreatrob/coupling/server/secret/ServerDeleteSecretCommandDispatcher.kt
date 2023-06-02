package com.zegreatrob.coupling.server.secret

import com.zegreatrob.coupling.action.DeleteSecretCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.repository.secret.SecretDelete

interface ServerDeleteSecretCommandDispatcher : DeleteSecretCommand.Dispatcher {

    val secretRepository: SecretDelete

    override suspend fun perform(command: DeleteSecretCommand) = secretRepository.deleteSecret(
        partyId = command.partyId,
        secretId = command.secret.id,
    ).successResult()
}
