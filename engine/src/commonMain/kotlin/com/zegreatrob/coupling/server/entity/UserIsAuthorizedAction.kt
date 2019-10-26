package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.tribe.UserAuthenticatedTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.UserPlayersSyntax

data class UserIsAuthorizedAction(val tribeId: TribeId)

interface UserIsAuthorizedActionDispatcher : UserAuthenticatedTribeIdSyntax, UserPlayersSyntax {

    suspend fun UserIsAuthorizedAction.perform(): Boolean = getUserPlayersAsync().await()
            .authenticatedTribeIds()
            .contains(tribeId)

}