package com.zegreatrob.coupling.server.action

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.secret.SecretSave

interface ServerCreateSecretCommandDispatcher : CreateSecretCommand.Dispatcher {
    val secretRepository: SecretSave
    val secretGenerator: SecretGenerator

    override suspend fun perform(command: CreateSecretCommand): Result<Pair<Secret, String>> {
        val secret = newSecret()
        val partyId = command.partyId
        secretRepository.save(partyId.with(secret))
        return (secret to secretGenerator.createSecret(partyId.with(secret)))
            .successResult()
    }

    private fun newSecret() = Secret("${uuid4()}")
}
