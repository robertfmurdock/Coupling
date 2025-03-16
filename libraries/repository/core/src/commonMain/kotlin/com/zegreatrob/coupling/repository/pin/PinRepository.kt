package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import kotools.types.text.NotBlankString

interface PinRepository :
    PinSave,
    PinGet,
    PinDelete

interface PinGet {
    suspend fun getPins(partyId: PartyId): List<PartyRecord<Pin>>
}

interface PinSave {
    suspend fun save(partyPin: PartyElement<Pin>)
}

interface PinDelete {
    suspend fun deletePin(partyId: PartyId, pinId: NotBlankString): Boolean
}
