package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax

interface ServerDeletePartyCommandDispatcher :
    DeletePartyCommand.Dispatcher,
    PartyIdDeleteSyntax,
    CurrentPartyIdSyntax,
    AuthenticatedUserSyntax,
    UserSaveSyntax {
    override suspend fun perform(command: DeletePartyCommand) = currentPartyId.deleteIt()
        .voidResult()
        .also {
            if (it is VoidResult.Accepted) {
                user
                    .copy(authorizedPartyIds = user.authorizedPartyIds.filter { id -> id != currentPartyId }.toSet())
                    .saveIfChanged()
            }
        }

    private suspend fun User.saveIfChanged() {
        if (this != user) {
            save()
        }
    }
}
