package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    suspend fun TribeId.load() = tribeRepository.getTribe(this@load)
}