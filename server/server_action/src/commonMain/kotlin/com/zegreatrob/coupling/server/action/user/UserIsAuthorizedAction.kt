package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayerIdsSyntax

data class UserIsAuthorizedAction(val tribeId: TribeId) : Action

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax, ActionLoggingSyntax {

    suspend fun UserIsAuthorizedAction.perform(): Boolean = logAsync {
        getUserPlayerIds()
            .authenticatedTribeIds()
            .contains(tribeId)
    }

}
