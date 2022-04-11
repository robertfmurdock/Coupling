package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PartyId

interface SpinDispatcher {
    suspend fun requestSpin(tribeId: PartyId, players: List<Player>, pins: List<Pin>): PairAssignmentDocument
}