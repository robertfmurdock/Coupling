package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository

class CompoundPinRepository(val repository1: PinRepository, val repository2: PinRepository) :
    PinRepository by repository1 {

    override suspend fun save(tribeIdPin: TribeIdPin) = arrayOf(repository1, repository2).forEach {
        it.save(tribeIdPin)
    }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = repository1.deletePin(tribeId, pinId).also {
        repository2.deletePin(tribeId, pinId)
    }
}
