package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pin.PinRepository

class CompoundPinRepository(val repository1: PinRepository, val repository2: PinRepository) :
    PinRepository by repository1 {

    override suspend fun save(tribeIdPin: PartyElement<Pin>) = arrayOf(repository1, repository2).forEach {
        it.save(tribeIdPin)
    }

    override suspend fun deletePin(tribeId: PartyId, pinId: String) = repository1.deletePin(tribeId, pinId).also {
        repository2.deletePin(tribeId, pinId)
    }
}
