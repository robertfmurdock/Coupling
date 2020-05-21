package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayerIdsSyntax

data class UserIsAuthorizedAction(val tribeId: TribeId) :
    SimpleSuspendResultAction<UserIsAuthorizedActionDispatcher, Boolean> {
    override val performFunc = link(UserIsAuthorizedActionDispatcher::perform)
}

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax {

    suspend fun perform(action: UserIsAuthorizedAction) = getUserPlayerIds()
        .authenticatedTribeIds()
        .contains(action.tribeId)
        .successResult()

}
