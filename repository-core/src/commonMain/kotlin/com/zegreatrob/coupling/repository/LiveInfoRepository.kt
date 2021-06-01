package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.model.tribe.TribeId

interface LiveInfoRepository {
    fun get(tribeId: TribeId): LiveInfo
    fun save(tribeId: TribeId, info: LiveInfo)
}