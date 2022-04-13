package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun PartyId.delete() = tribeRepository.delete(this)
}
