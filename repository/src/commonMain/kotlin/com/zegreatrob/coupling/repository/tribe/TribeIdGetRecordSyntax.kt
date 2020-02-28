package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdGetRecordSyntax {
    val tribeRepository: TribeGet
    suspend fun TribeId.loadRecord() = tribeRepository.getTribeRecord(this)
}