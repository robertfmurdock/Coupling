package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.party.Party

interface TribeSaveSyntax {
    val tribeRepository: TribeSave
    suspend fun Party.save() = tribeRepository.save(this)
}