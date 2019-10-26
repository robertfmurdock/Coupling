package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.core.entity.tribe.TribeId

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    fun TribeId.loadAsync() = tribeRepository.getTribeAsync(this)
}