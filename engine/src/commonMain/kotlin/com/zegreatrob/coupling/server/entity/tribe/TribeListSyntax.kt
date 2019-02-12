package com.zegreatrob.coupling.server.entity.tribe

interface TribeListSyntax {
    val tribeRepository: TribeRepository
    fun getTribesAsync() = tribeRepository.getTribesAsync()
}