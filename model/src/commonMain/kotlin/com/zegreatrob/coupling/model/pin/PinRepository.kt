package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.tribe.TribeId

interface PinRepository : PinSaver, PinGetter, PinDeleter

interface PinGetter {
    suspend fun getPins(tribeId: TribeId): List<Pin>
}

interface PinSaver {
    suspend fun save(tribeIdPin: TribeIdPin)
}

interface PinDeleter {
    suspend fun deletePin(tribeId: TribeId, pinId: String): Boolean
}
