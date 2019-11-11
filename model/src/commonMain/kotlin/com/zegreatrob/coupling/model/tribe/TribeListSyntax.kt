package com.zegreatrob.coupling.model.tribe

interface TribeListSyntax {
    val tribeRepository: TribeListGet
    suspend fun getTribes() = tribeRepository.getTribes()
}