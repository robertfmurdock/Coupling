package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ContributorPlayerQuery(val partyId: PartyId, val email: String) {
    interface Dispatcher : PartyPlayersSyntax {
        suspend fun perform(query: ContributorPlayerQuery): PartyRecord<Player>? = query.partyId.loadPlayers()
            .find { it.element.matches(query.email) }
    }
}
