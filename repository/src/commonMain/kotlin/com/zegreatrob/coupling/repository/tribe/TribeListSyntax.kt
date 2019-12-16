package com.zegreatrob.coupling.repository.tribe

interface TribeListSyntax {
    val tribeRepository: TribeListGet
    suspend fun getTribes() = tribeRepository.getTribes()
}