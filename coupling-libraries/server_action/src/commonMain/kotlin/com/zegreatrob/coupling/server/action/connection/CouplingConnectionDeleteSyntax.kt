package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionDeleteSyntax {
    val liveInfoRepository: LiveInfoRepository

    suspend fun deleteConnection(partyId: PartyId, connectionId: String) {
        liveInfoRepository.delete(partyId, connectionId)
    }
}
