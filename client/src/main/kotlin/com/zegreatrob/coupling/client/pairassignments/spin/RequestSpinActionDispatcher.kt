package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.sdk.ServerRequestSpin
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId

data class RequestSpinAction(val tribeId: TribeId, val players: List<Player>)

interface RequestSpinActionDispatcher : ServerRequestSpin {

    suspend fun RequestSpinAction.perform(): PairAssignmentDocument =
        requestSpinAsync(tribeId, players)
            .await()

}
