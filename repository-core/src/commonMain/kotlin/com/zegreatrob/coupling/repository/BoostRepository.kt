package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId

interface BoostRepository {
    fun get(): Record<Boost>?
    fun save(boost: Boost)
    fun getByTribeId(tribeId: TribeId): Record<Boost>?
    fun allLatestRecords(): List<Record<Boost>>
}