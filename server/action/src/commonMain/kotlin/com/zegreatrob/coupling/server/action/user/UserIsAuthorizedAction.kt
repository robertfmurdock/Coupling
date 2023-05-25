package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.party.UserAuthenticatedPartyIdSyntax
import com.zegreatrob.coupling.server.action.party.UserPlayerIdsSyntax

data class UserIsAuthorizedAction(val partyId: PartyId) :
    SimpleSuspendResultAction<UserIsAuthorizedActionDispatcher, Boolean> {
    override val performFunc = link(UserIsAuthorizedActionDispatcher::perform)
}

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedPartyIdSyntax, UserPlayerIdsSyntax {

    suspend fun perform(action: UserIsAuthorizedAction) = getUserPlayerIds()
        .authenticatedPartyIds()
        .contains(action.partyId)
        .successResult()
}
