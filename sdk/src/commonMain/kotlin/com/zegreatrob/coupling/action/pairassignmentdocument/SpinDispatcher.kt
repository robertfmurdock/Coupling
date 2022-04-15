package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId

interface SpinDispatcher {
    suspend fun requestSpin(partyId: PartyId, players: List<Player>, pins: List<Pin>): PairAssignmentDocument
}