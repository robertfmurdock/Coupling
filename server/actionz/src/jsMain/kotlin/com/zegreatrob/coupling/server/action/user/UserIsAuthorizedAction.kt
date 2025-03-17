package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.UserAuthenticatedPartyIdSyntax
import com.zegreatrob.coupling.server.action.party.UserPlayerIdsSyntax

data class UserIsAuthorizedAction(val partyId: PartyId) : SimpleSuspendResultAction<UserIsAuthorizedAction.Dispatcher, Boolean> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        UserAuthenticatedPartyIdSyntax,
        UserPlayerIdsSyntax {

        suspend fun perform(action: UserIsAuthorizedAction) = getUserPlayerIds(currentUser.email)
            .authenticatedPartyIds()
            .contains(action.partyId)
            .successResult()
    }
}
