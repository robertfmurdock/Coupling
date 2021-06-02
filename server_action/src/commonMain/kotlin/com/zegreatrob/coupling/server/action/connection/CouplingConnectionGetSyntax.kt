package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionGetSyntax {
    val liveInfoRepository: LiveInfoRepository
    suspend fun TribeId.loadConnections() = liveInfoRepository.connectionList(this)
}