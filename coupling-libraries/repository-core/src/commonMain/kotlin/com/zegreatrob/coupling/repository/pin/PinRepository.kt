package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.PartyId

interface PinRepository : PinSave,
    PinGet,
    PinDelete

interface PinGet {
    suspend fun getPins(tribeId: PartyId): List<PartyRecord<Pin>>
}

interface PinSave {
    suspend fun save(tribeIdPin: PartyElement<Pin>)
}

interface PinDelete {
    suspend fun deletePin(tribeId: PartyId, pinId: String): Boolean
}
