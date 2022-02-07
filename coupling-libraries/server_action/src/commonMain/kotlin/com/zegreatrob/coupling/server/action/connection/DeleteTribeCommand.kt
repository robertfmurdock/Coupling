package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax

object DeleteTribeCommand : SimpleSuspendResultAction<DeleteTribeCommandDispatcher, Unit> {
    override val performFunc = link(DeleteTribeCommandDispatcher::perform)
}

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax, CurrentTribeIdSyntax, AuthenticatedUserSyntax,
    UserSaveSyntax {
    suspend fun perform(command: DeleteTribeCommand) = currentTribeId.delete().deletionResult("Tribe")
        .also {
            if (it is SuccessfulResult) {
                user
                    .copy(authorizedTribeIds = user.authorizedTribeIds.filter { id -> id != currentTribeId }.toSet())
                    .saveIfChanged()
            }
        }

    private suspend fun User.saveIfChanged() {
        if (this != user) {
            save()
        }
    }
}
