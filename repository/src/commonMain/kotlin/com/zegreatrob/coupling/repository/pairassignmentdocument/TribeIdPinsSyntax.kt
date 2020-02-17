package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPinsSyntax: TribeIdPinRecordsSyntax {
    suspend fun TribeId.getPins() = getPinRecords().map { it.data.pin }
}

