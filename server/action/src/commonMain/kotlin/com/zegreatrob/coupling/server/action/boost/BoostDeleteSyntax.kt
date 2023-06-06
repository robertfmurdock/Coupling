package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.repository.BoostDelete

interface BoostDeleteSyntax {

    val boostRepository: BoostDelete

    suspend fun deleteIt() {
        boostRepository.deleteIt()
    }
}
