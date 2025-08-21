package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.UserIdProvider

interface UserAuthenticatedPartyIdSyntax :
    UserIdProvider,
    AuthorizedPartyIdsProvider {

    suspend fun List<PartyRecord<Player>>.authenticatedPartyIds() = map { it.data.partyId } + authorizedPartyIds()
}
