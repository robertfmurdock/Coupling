package com.zegreatrob.coupling.model.tribe

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    suspend fun TribeId.load() = tribeRepository.getTribe(this@load)
}