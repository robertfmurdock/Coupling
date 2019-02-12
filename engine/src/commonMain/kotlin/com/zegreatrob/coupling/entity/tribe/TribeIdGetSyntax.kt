package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.tribe.TribeId

interface TribeIdGetSyntax {
    val tribeRepository: TribeRepository
    fun TribeId.loadAsync() = tribeRepository.getTribeAsync(this)
}