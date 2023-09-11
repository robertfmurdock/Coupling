package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyBoostQuery(val partyId: PartyId) {
    fun interface Dispatcher {
        suspend fun perform(query: PartyBoostQuery): Record<Boost>?
    }
}
