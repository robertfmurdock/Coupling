package com.zegreatrob.coupling.model.tribe

interface TribeSaveSyntax {
    val tribeRepository: TribeSave
    suspend fun Tribe.save() = tribeRepository.save(this)
}