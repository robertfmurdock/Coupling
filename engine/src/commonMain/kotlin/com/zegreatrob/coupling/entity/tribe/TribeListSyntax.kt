package com.zegreatrob.coupling.entity.tribe

interface TribeListSyntax {
    val tribeRepository: TribeRepository
    fun getTribesAsync() = tribeRepository.getTribesAsync()
}