package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayerIdsSyntax

data class UserIsAuthorizedAction(val tribeId: TribeId)

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayerIdsSyntax {

    suspend fun UserIsAuthorizedAction.perform(): Boolean = getUserPlayerIds()
        .authenticatedTribeIds()
        .contains(tribeId)

}