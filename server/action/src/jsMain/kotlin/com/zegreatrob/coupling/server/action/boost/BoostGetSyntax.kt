package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.BoostGet

interface BoostGetSyntax {
    val boostRepository: BoostGet
    suspend fun load(): Record<Boost>? = boostRepository.get()
}
