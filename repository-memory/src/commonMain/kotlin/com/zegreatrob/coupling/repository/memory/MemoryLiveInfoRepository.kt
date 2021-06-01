package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository

val infoMap = mutableListOf<CouplingConnection>()

class MemoryLiveInfoRepository : LiveInfoRepository {
    override suspend fun get(tribeId: TribeId) = infoMap.filter { it.tribeId == tribeId }.sortedBy { it.connectionId }
    override suspend fun save(connection: CouplingConnection) {
        infoMap.add(connection)
    }
    override suspend fun delete(tribeId: TribeId, connectionId: String) {
        infoMap.removeAll { it.connectionId == connectionId }
    }
}
