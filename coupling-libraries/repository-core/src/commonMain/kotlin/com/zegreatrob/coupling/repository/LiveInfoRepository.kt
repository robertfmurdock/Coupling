package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.tribe.PartyId

interface LiveInfoRepository {
    suspend fun connectionList(tribeId: PartyId): List<CouplingConnection>
    suspend fun get(connectionId: String): CouplingConnection?
    suspend fun save(connection: CouplingConnection)
    suspend fun delete(tribeId: PartyId, connectionId: String)
}
