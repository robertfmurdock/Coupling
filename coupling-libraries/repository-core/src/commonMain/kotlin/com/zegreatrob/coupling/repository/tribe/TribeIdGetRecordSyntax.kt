package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.PartyId

interface TribeIdGetRecordSyntax {
    val tribeRepository: TribeGet
    suspend fun PartyId.loadRecord() = tribeRepository.getTribeRecord(this)
}