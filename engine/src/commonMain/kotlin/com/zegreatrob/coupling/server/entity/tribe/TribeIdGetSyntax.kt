package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    fun TribeId.loadAsync() = tribeRepository.getTribeAsync(this)
}