package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.BoostGetByPartyId

interface PartyIdBoostSyntax {
    val boostRepository: BoostGetByPartyId
    suspend fun PartyId.boost(): Record<Boost>? = boostRepository.getByPartyId(this)
}
