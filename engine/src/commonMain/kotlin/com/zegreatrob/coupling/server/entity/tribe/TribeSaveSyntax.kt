package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.KtTribe

interface TribeSaveSyntax {
    val tribeRepository: TribeRepository
    suspend fun KtTribe.save() = tribeRepository.save(this)
}