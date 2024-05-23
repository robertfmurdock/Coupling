package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionGetSyntax {
    val liveInfoRepository: LiveInfoRepository
    suspend fun PartyId.loadConnections() = liveInfoRepository.connectionList(this)
}
