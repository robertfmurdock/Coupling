package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.UserSecretGenerator
import kotlin.time.Clock

interface ServerCreateConnectUserSecretCommandDispatcher :
    CreateConnectUserSecretCommand.Dispatcher,
    CurrentUserProvider,
    UserSaveSyntax {
    val secretGenerator: UserSecretGenerator

    override suspend fun perform(command: CreateConnectUserSecretCommand): Pair<Secret, String>? {
        val secretId = SecretId.new()
        val token = secretGenerator.createSecret(currentUser.id to secretId)
        currentUser.copy(connectSecretId = secretId)
            .save()
        return Secret(
            id = secretId,
            description = "Single-use user connection secret",
            createdTimestamp = Clock.System.now(),
            lastUsedTimestamp = null,
        ) to token
    }
}
