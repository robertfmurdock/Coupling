package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.SecretValidator
import kotools.types.text.toNotBlankString

interface ServerConnectUserCommandDispatcher :
    ConnectUserCommand.Dispatcher,
    UserSaveSyntax,
    CurrentUserProvider {

    val secretGenerator: SecretValidator

    override suspend fun perform(command: ConnectUserCommand): Boolean? {
        val result = secretGenerator.validateSubject(command.token)
            ?: return false
        val (secretId, subject) = result
        val targetUserEmail = subject.toNotBlankString().getOrNull()
        val targetUserDetails = targetUserEmail?.let { userRepository.getUsersWithEmail(it) }?.firstOrNull()?.data
            ?: return false
        return userRepository.connectUser(currentUser, targetUserDetails, secretId)
    }
}

interface ServerDisconnectUserCommandDispatcher :
    DisconnectUserCommand.Dispatcher,
    CurrentUserProvider,
    UserSaveSyntax {
    override suspend fun perform(command: DisconnectUserCommand): VoidResult {
        currentUser.copy(connectedEmails = currentUser.connectedEmails.minus(command.email))
            .save()
        val targetUserDetails = userRepository.getUsersWithEmail(command.email).firstOrNull()?.data
        targetUserDetails
            ?.copy(connectedEmails = targetUserDetails.connectedEmails.minus(currentUser.email))
            ?.save()

        return VoidResult.Accepted
    }
}
