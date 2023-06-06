package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.action.party.ServerSavePartyCommandDispatcher

interface PartyDispatcher :
    ServerSavePartyCommandDispatcher,
    PartyListQuery.Dispatcher,
    PartyQuery.Dispatcher {
    override val partyRepository: PartyRepository
}
