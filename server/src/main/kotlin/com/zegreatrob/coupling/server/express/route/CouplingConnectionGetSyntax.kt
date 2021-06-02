package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionGetSyntax {
    val liveInfoRepository: LiveInfoRepository
    suspend fun TribeId.loadConnections() = liveInfoRepository.get(this)
}