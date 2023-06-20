package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

typealias PartyPlayerData = Triple<PartyDetails, List<Player>, Player>

data class PartyPlayerQuery(val partyId: PartyId, val playerId: String?) :
    SimpleSuspendAction<PartyPlayerQuery.Dispatcher, PartyPlayerData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyPlayerQuery): Triple<PartyDetails, List<Player>, Player>?
    }
}
