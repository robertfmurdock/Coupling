package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.SdkSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class RequestSpinAction(val partyId: PartyId, val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendAction<RequestSpinActionDispatcher, PairAssignmentDocument> {
    override val performFunc = link(RequestSpinActionDispatcher::perform)
}

interface RequestSpinActionDispatcher : SdkSyntax {
    suspend fun perform(action: RequestSpinAction) = with(action) { sdk.requestSpin(partyId, players, pins) }
}
