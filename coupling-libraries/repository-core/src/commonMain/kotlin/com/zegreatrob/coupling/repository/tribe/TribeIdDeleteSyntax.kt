package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.PartyId

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun PartyId.delete() = tribeRepository.delete(this)
}
