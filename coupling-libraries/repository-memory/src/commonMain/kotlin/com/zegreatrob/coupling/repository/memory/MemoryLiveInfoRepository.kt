package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.LiveInfoRepository

val infoMap = mutableListOf<CouplingConnection>()

class MemoryLiveInfoRepository : LiveInfoRepository {
    override suspend fun get(connectionId: String) = infoMap.find { it.connectionId == connectionId }
    override suspend fun save(connection: CouplingConnection) = infoMap.add(connection).let { }
    override suspend fun delete(tribeId: PartyId, connectionId: String) =
        infoMap.removeAll { it.connectionId == connectionId }.let { }

    override suspend fun connectionList(tribeId: PartyId) = infoMap.filter { it.partyId == tribeId }
        .sortedBy { it.connectionId }
}
