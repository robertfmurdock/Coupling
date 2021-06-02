package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.repository.LiveInfoRepository

interface CouplingConnectionSaveSyntax {
    val liveInfoRepository: LiveInfoRepository

    suspend fun CouplingConnection.save() {
        liveInfoRepository.save(this)
    }
}