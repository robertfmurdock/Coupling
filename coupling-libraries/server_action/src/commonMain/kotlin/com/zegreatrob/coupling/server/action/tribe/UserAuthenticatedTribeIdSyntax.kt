package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyElement
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.user.UserAuthorizedTribeIdsSyntax
import com.zegreatrob.coupling.model.user.UserIdSyntax

interface UserAuthenticatedTribeIdSyntax : UserIdSyntax, UserAuthorizedTribeIdsSyntax {

    fun List<PartyElement<String>>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<PartyElement<String>>.authenticatedTribeIds() = map { it.id } + userAuthorizedTribeIds()

    private fun List<PartyId>.authenticatedFilter(): (Record<Party>) -> Boolean = { contains(it.data.id) }
}