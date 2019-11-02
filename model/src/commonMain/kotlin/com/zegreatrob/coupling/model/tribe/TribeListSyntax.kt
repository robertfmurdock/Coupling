package com.zegreatrob.coupling.model.tribe

interface TribeListSyntax {
    val tribeRepository: TribeListGet
    fun getTribesAsync() = tribeRepository.getTribesAsync()
}