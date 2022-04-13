package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserAuthorizedPartyIdsSyntax
import com.zegreatrob.coupling.model.user.UserIdSyntax

interface UserAuthenticatedPartyIdSyntax : UserIdSyntax, UserAuthorizedPartyIdsSyntax {

    fun List<PartyElement<String>>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<PartyElement<String>>.authenticatedTribeIds() = map { it.id } + userAuthorizedPartyIds()

    private fun List<PartyId>.authenticatedFilter(): (Record<Party>) -> Boolean = { contains(it.data.id) }
}