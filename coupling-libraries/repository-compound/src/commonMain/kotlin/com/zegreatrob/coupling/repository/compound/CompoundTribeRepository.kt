package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeRepository

class CompoundTribeRepository(private val repository1: TribeRepository, private val repository2: TribeRepository) :
    TribeRepository by repository1 {

    override suspend fun save(party: Party) = arrayOf(repository1, repository2).forEach { it.save(party) }

    override suspend fun delete(tribeId: PartyId) = repository1.delete(tribeId)
        .also { repository2.delete(tribeId) }

}
