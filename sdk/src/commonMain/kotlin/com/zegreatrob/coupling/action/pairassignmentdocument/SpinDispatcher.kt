package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

interface SpinDispatcher {
    suspend fun requestSpin(partyId: PartyId, players: List<Player>, pins: List<Pin>): PairAssignmentDocument
}
