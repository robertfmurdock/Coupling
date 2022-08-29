package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.server.action.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.server.action.party.PartyQueryDispatcher
import com.zegreatrob.coupling.server.action.party.SavePartyCommandDispatcher

interface PartyDispatcher :
    SavePartyCommandDispatcher,
    PartyListQueryDispatcher,
    PartyQueryDispatcher
