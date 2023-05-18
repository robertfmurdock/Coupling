package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

typealias PartyPlayerData = Triple<Party, List<Player>, Player>

data class PartyPlayerQuery(val partyId: PartyId, val playerId: String?) :
    SimpleSuspendAction<PartyPlayerQuery.Dispatcher, PartyPlayerData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyPlayerQuery): Triple<Party, List<Player>, Player>?
    }
}
