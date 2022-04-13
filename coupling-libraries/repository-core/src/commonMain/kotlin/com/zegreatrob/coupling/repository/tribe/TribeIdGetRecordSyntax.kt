package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdGetRecordSyntax {
    val tribeRepository: TribeGet
    suspend fun PartyId.loadRecord() = tribeRepository.getTribeRecord(this)
}