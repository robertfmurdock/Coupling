package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.UserAuthorizedTribeIdsSyntax
import com.zegreatrob.coupling.action.UserEmailSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId

interface UserAuthenticatedTribeIdSyntax : UserEmailSyntax, UserAuthorizedTribeIdsSyntax {

    fun List<TribeIdPlayer>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<TribeIdPlayer>.authenticatedTribeIds() = map { it.tribeId } + userAuthorizedTribeIds()

    private fun List<TribeId>.authenticatedFilter(): (KtTribe) -> Boolean = { contains(it.id) }
}