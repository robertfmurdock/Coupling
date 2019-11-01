package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.UserAuthorizedTribeIdsSyntax
import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId

interface UserAuthenticatedTribeIdSyntax : UserEmailSyntax, UserAuthorizedTribeIdsSyntax {

    fun List<TribeIdPlayer>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<TribeIdPlayer>.authenticatedTribeIds() = map { it.tribeId } + userAuthorizedTribeIds()

    private fun List<TribeId>.authenticatedFilter(): (KtTribe) -> Boolean = { contains(it.id) }
}