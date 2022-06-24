package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class RequestSpinAction(val partyId: PartyId, val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendAction<RequestSpinActionDispatcher, PairAssignmentDocument> {
    override val performFunc = link(RequestSpinActionDispatcher::perform)
}

interface RequestSpinActionDispatcher {
    suspend fun perform(action: RequestSpinAction): PairAssignmentDocument
}
