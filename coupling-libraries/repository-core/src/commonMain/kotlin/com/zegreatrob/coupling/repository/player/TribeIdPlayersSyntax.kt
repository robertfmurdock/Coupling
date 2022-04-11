package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.tribe.PartyId

interface TribeIdPlayersSyntax : TribeIdPlayerRecordsListSyntax {
    suspend fun PartyId.getPlayerList() = getPlayerRecords().elements
}
