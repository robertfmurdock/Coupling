package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.user.AuthorizedPartyIdsProvider
import com.zegreatrob.coupling.model.user.UserIdProvider

interface UserAuthenticatedPartyIdSyntax :
    UserIdProvider,
    AuthorizedPartyIdsProvider {

    fun List<PartyElement<PlayerId>>.authenticatedPartyIds() = map { it.partyId } + authorizedPartyIds()
}
