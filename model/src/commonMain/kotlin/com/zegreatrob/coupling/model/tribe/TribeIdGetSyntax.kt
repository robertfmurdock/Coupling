package com.zegreatrob.coupling.model.tribe

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    fun TribeId.loadAsync() = tribeRepository.getTribeAsync(this)
}