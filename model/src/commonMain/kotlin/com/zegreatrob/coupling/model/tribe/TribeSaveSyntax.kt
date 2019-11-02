package com.zegreatrob.coupling.model.tribe

interface TribeSaveSyntax {
    val tribeRepository: TribeSave
    suspend fun KtTribe.save() = tribeRepository.save(this)
}