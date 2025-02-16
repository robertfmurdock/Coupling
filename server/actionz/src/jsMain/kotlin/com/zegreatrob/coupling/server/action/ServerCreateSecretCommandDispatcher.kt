package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.secret.SecretSave
import kotlinx.datetime.Clock
import kotlin.uuid.Uuid

interface ServerCreateSecretCommandDispatcher : CreateSecretCommand.Dispatcher {
    val secretRepository: SecretSave
    val secretGenerator: SecretGenerator

    override suspend fun perform(command: CreateSecretCommand): Pair<Secret, String> = command.partySecret()
        .save()
        .oneTimeSecretValueGeneration()

    private suspend fun PartyElement<Secret>.oneTimeSecretValueGeneration() = Pair(
        first = element,
        second = secretGenerator.createSecret(this),
    )

    private suspend fun PartyElement<Secret>.save() = apply { secretRepository.save(this) }

    private fun CreateSecretCommand.partySecret(): PartyElement<Secret> = partyId.with(
        Secret(
            id = "${Uuid.random()}",
            description = description,
            createdTimestamp = Clock.System.now(),
            lastUsedTimestamp = null,
        ),
    )
}
