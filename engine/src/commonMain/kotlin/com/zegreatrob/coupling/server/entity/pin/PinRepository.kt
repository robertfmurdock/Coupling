package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.pin.TribeIdPin
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PinRepository : PinSaver, PinGetter, PinDeleter

interface PinGetter {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
}

interface PinSaver {
    suspend fun save(tribeIdPin: TribeIdPin)
}

interface PinDeleter {
    suspend fun deletePin(pinId: String): Boolean
}
