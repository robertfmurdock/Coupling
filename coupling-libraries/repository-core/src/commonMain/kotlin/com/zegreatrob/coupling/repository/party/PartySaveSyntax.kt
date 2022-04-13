package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.Party

interface PartySaveSyntax {
    val partyRepository: PartySave
    suspend fun Party.save() = partyRepository.save(this)
}