package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdPlayersSyntax : TribeIdPlayerRecordsListSyntax {
    suspend fun PartyId.getPlayerList() = getPlayerRecords().elements
}
