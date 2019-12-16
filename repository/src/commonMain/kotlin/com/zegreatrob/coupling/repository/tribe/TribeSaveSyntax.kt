package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.Tribe

interface TribeSaveSyntax {
    val tribeRepository: TribeSave
    suspend fun Tribe.save() = tribeRepository.save(this)
}