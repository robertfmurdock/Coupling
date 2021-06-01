package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.LiveInfo
import com.zegreatrob.coupling.repository.LiveInfoRepository


val infoMap = mutableMapOf<TribeId, LiveInfo>()

class MemoryLiveInfoRepository : LiveInfoRepository {
    override fun get(tribeId: TribeId) = infoMap[tribeId] ?: LiveInfo(emptyList())
    override fun save(tribeId: TribeId, info: LiveInfo) {
        infoMap[tribeId] = info
    }
}
