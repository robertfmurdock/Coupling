package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.SdkSyntax

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>, val pins: List<Pin>) :
    SimpleSuspendResultAction<RequestSpinActionDispatcher, PairAssignmentDocument> {
    override val performFunc = link(RequestSpinActionDispatcher::perform)
}

interface RequestSpinActionDispatcher : SdkSyntax {
    suspend fun perform(action: RequestSpinAction) = with(action) { sdk.requestSpin(tribeId, players, pins) }
        .successResult()
}
