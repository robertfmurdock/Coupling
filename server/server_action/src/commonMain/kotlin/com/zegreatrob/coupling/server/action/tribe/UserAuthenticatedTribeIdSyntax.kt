package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.UserAuthorizedTribeIdsSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface UserAuthenticatedTribeIdSyntax : UserEmailSyntax, UserAuthorizedTribeIdsSyntax {

    fun List<TribeIdPlayer>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<TribeIdPlayer>.authenticatedTribeIds() = map { it.tribeId } + userAuthorizedTribeIds()

    private fun List<TribeId>.authenticatedFilter(): (Tribe) -> Boolean = { contains(it.id) }
}