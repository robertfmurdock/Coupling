package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.AuthorizedPartyIdsProvider
import com.zegreatrob.coupling.model.user.UserIdProvider

interface UserAuthenticatedPartyIdSyntax :
    UserIdProvider,
    AuthorizedPartyIdsProvider {

    fun List<PartyRecord<Player>>.authenticatedPartyIds() = map { it.data.partyId } + authorizedPartyIds()
}
