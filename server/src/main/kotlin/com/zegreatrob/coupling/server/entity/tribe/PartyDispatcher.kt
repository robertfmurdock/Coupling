package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.tribe.SavePartyCommandDispatcher
import com.zegreatrob.coupling.server.action.tribe.PartyListQueryDispatcher
import com.zegreatrob.coupling.server.action.tribe.PartyQueryDispatcher

interface PartyDispatcher : SavePartyCommandDispatcher,
    PartyListQueryDispatcher,
    PartyQueryDispatcher
