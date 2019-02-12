package com.zegreatrob.coupling.entity

import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

interface PinRepository {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
}