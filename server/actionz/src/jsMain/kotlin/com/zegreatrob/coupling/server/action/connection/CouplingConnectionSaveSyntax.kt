package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionSaveSyntax {
    val liveInfoRepository: LiveInfoRepository

    suspend fun CouplingConnection.save() {
        liveInfoRepository.save(this)
    }
}
