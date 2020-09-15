package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.express.route.LiveInfo
import com.zegreatrob.coupling.server.express.route.LiveInfoRepository


val infoMap = mutableMapOf<TribeId, LiveInfo>()

class MemoryLiveInfoRepository : LiveInfoRepository {
    override fun get(tribeId: TribeId) = infoMap[tribeId] ?: LiveInfo(emptyList(), null)
    override fun save(tribeId: TribeId, info: LiveInfo) {
        infoMap[tribeId] = info
    }
}
