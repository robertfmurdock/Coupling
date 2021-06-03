package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionDeleteSyntax {
    val liveInfoRepository: LiveInfoRepository

    suspend fun deleteConnection(tribeId: TribeId, connectionId: String) {
        liveInfoRepository.delete(tribeId, connectionId)
    }
}