package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.party.SavePartyCommandDispatcher
import com.zegreatrob.coupling.server.action.party.PartyListQueryDispatcher
import com.zegreatrob.coupling.server.action.party.PartyQueryDispatcher

interface PartyDispatcher : SavePartyCommandDispatcher,
    PartyListQueryDispatcher,
    PartyQueryDispatcher
