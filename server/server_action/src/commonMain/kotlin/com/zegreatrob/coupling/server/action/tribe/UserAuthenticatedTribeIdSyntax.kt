package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.UserAuthorizedTribeIdsSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface UserAuthenticatedTribeIdSyntax : UserEmailSyntax, UserAuthorizedTribeIdsSyntax {

    fun List<TribeElement<String>>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<TribeElement<String>>.authenticatedTribeIds() = map { it.id } + userAuthorizedTribeIds()

    private fun List<TribeId>.authenticatedFilter(): (Tribe) -> Boolean = { contains(it.id) }
}