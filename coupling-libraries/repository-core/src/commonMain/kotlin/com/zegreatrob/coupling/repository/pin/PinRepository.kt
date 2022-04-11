package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.PartyId

interface PinRepository : PinSave,
    PinGet,
    PinDelete

interface PinGet {
    suspend fun getPins(tribeId: PartyId): List<TribeRecord<Pin>>
}

interface PinSave {
    suspend fun save(tribeIdPin: TribeIdPin)
}

interface PinDelete {
    suspend fun deletePin(tribeId: PartyId, pinId: String): Boolean
}
