package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId

interface PinRepository : PinSave,
    PinGet,
    PinDelete

interface PinGet {
    suspend fun getPins(tribeId: TribeId): List<Record<TribeIdPin>>
}

interface PinSave {
    suspend fun save(tribeIdPin: TribeIdPin)
}

interface PinDelete {
    suspend fun deletePin(tribeId: TribeId, pinId: String): Boolean
}
