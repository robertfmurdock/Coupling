package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.tribe.KtTribe

interface TribeSaveSyntax {
    val tribeRepository: TribeRepository
    suspend fun KtTribe.save() = tribeRepository.save(this)
}