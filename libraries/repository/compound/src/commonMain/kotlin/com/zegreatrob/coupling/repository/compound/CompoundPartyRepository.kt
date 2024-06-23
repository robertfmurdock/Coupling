package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyRepository

class CompoundPartyRepository(private val repository1: PartyRepository, private val repository2: PartyRepository) : PartyRepository by repository1 {

    override suspend fun save(party: PartyDetails) = arrayOf(repository1, repository2).forEach { it.save(party) }

    override suspend fun deleteIt(partyId: PartyId) = repository1.deleteIt(partyId)
        .also { repository2.deleteIt(partyId) }
}
