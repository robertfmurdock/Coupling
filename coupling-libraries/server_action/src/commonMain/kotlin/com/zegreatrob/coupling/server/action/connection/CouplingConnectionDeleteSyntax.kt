package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionDeleteSyntax {
    val liveInfoRepository: LiveInfoRepository

    suspend fun deleteConnection(tribeId: PartyId, connectionId: String) {
        liveInfoRepository.delete(tribeId, connectionId)
    }
}