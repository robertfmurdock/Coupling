package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId

interface BoostRepository {
    suspend fun get(): Record<Boost>?
    suspend fun save(boost: Boost)
    suspend fun getByTribeId(tribeId: TribeId): Record<Boost>?
}