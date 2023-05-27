package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.action.party.PartyQuery
import com.zegreatrob.coupling.server.action.party.SavePartyCommand

interface PartyDispatcher :
    SavePartyCommand.Dispatcher,
    PartyListQuery.Dispatcher,
    PartyQuery.Dispatcher
