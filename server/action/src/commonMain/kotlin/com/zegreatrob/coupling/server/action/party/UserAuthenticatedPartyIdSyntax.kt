package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.AuthorizedPartyIdsProvider
import com.zegreatrob.coupling.model.user.UserIdProvider

interface UserAuthenticatedPartyIdSyntax : UserIdProvider, AuthorizedPartyIdsProvider {

    fun List<PartyElement<String>>.authenticatedFilter() = authenticatedPartyIds().authenticatedFilter()

    fun List<PartyElement<String>>.authenticatedPartyIds() = map { it.partyId } + authorizedPartyIds()

    private fun List<PartyId>.authenticatedFilter(): (Record<PartyDetails>) -> Boolean = { contains(it.data.id) }
}
