package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PinRepository : PinSaver, PinGetter, PinDeleter

interface PinGetter {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
}

interface PinSaver {
    suspend fun save(tribeIdPin: TribeIdPin)
}

interface PinDeleter {
    suspend fun deletePin(tribeId: TribeId, pinId: String): Boolean
}
