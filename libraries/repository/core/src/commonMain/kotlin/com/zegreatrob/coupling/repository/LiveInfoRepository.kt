package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.party.PartyId

interface LiveInfoRepository {
    suspend fun connectionList(partyId: PartyId): List<CouplingConnection>
    suspend fun get(connectionId: String): CouplingConnection?
    suspend fun save(connection: CouplingConnection)
    suspend fun deleteIt(partyId: PartyId, connectionId: String)
}
