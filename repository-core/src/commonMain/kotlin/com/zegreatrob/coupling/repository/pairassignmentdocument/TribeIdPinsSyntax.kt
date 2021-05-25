package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPinsSyntax : TribeIdPinRecordsSyntax {
    suspend fun TribeId.getPins() = getPinRecords().elements
}

