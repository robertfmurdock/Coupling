package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.action.tribe.UserPlayersSyntax

data class UserIsAuthorizedAction(val tribeId: TribeId)

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayersSyntax {

    suspend fun UserIsAuthorizedAction.perform(): Boolean = getUserPlayersAsync()
            .authenticatedTribeIds()
            .contains(tribeId)

}