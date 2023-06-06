package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.repository.BoostSave

interface BoostSaveSyntax {

    val boostRepository: BoostSave

    suspend fun Boost.save() {
        boostRepository.save(this)
    }
}
