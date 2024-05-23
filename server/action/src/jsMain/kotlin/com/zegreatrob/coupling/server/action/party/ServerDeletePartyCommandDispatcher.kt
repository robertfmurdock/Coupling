package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax

interface ServerDeletePartyCommandDispatcher :
    DeletePartyCommand.Dispatcher,
    PartyIdDeleteSyntax,
    CurrentPartyIdSyntax,
    CurrentUserProvider,
    UserSaveSyntax {
    override suspend fun perform(command: DeletePartyCommand) = currentPartyId.deleteIt()
        .voidResult()
        .also {
            if (it is VoidResult.Accepted) {
                currentUser
                    .copy(authorizedPartyIds = currentUser.authorizedPartyIds.filter { id -> id != currentPartyId }.toSet())
                    .saveIfChanged()
            }
        }

    private suspend fun UserDetails.saveIfChanged() {
        if (this != currentUser) {
            save()
        }
    }
}
